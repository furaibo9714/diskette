package com.michaldrabik.ui_base.floppy

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Type
import com.michaldrabik.data_remote.floppy.api.FloppyService
import com.michaldrabik.data_remote.floppy.model.FloppyDiscoverHiddenRequest
import com.michaldrabik.data_remote.floppy.model.FloppyEmptyRequest
import com.michaldrabik.data_remote.floppy.model.FloppyScoreUpdateRequest
import com.michaldrabik.data_remote.floppy.model.FloppyStatusUpdateRequest
import com.michaldrabik.data_remote.floppy.model.FloppyTrackRequest
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drains the outbound [FloppySyncQueue] and pushes each item to Floppy. Unlike Trakt, Floppy has
 * no bulk sync endpoint, so items are pushed one at a time. A failed item is dropped rather than
 * retried indefinitely - the next full import (see imports/) will reconcile any drift.
 */
@Singleton
class FloppySyncRunner @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
) {

  var progressListener: (suspend (count: Int, total: Int) -> Unit)? = null

  suspend fun run(): Int {
    if (!connectionManager.isConfigured()) return 0

    val items = localSource.floppySyncQueue.getAll()
    if (items.isEmpty()) return 0

    val service = connectionManager.service()
    var pushed = 0

    items.forEachIndexed { index, item ->
      progressListener?.invoke(index + 1, items.size)
      try {
        push(service, item)
        pushed++
      } catch (error: Throwable) {
        Timber.w(error, "Failed to push Floppy sync item: $item")
      }
      localSource.floppySyncQueue.delete(listOf(item))
    }

    return pushed
  }

  private suspend fun push(
    service: FloppyService,
    item: FloppySyncQueue,
  ) {
    val isAdd = item.operation == Operation.ADD.slug
    when (item.type) {
      Type.SHOW_WATCHLIST.slug -> pushWatchlist(service, MEDIA_TYPE_TV, item, isAdd)
      Type.MOVIE_WATCHLIST.slug -> pushWatchlist(service, MEDIA_TYPE_MOVIE, item, isAdd)
      Type.MOVIE_WATCHED.slug -> pushMovieWatched(service, item, isAdd)
      Type.EPISODE_WATCHED.slug -> pushEpisodeWatched(service, item, isAdd)
      Type.SHOW_RATING.slug -> pushMediaRating(service, MEDIA_TYPE_TV, item, isAdd)
      Type.MOVIE_RATING.slug -> pushMediaRating(service, MEDIA_TYPE_MOVIE, item, isAdd)
      Type.SEASON_RATING.slug -> pushSeasonRating(service, item, isAdd)
      Type.EPISODE_RATING.slug -> pushEpisodeRating(service, item, isAdd)
      Type.LIST_ITEM_SHOW.slug -> pushListItem(service, MEDIA_TYPE_TV, item, isAdd)
      Type.LIST_ITEM_MOVIE.slug -> pushListItem(service, MEDIA_TYPE_MOVIE, item, isAdd)
      Type.LIST_DELETE.slug -> pushListDelete(service, item)
      Type.SHOW_HIDDEN.slug -> pushHidden(service, MEDIA_TYPE_TV, item, isAdd)
      Type.MOVIE_HIDDEN.slug -> pushHidden(service, MEDIA_TYPE_MOVIE, item, isAdd)
    }
  }

  /**
   * Hiding/unhiding is `POST /api/v1/discover/hidden/` with `{"item_id": <int>, "action": "hide"|"unhide"}`
   * - `item_id` is Floppy's own internal `Item` row id (same value as [getMediaDetail]'s `id`), not
   * the (media_type, source, media_id) triplet every other call here uses. Confirmed by reading
   * Floppy's source (`DiscoverHiddenView.post`) - its OpenAPI schema documents no request body at
   * all for this endpoint. Untracked/uncataloged items resolve to a `null` id (same "not tracked
   * yet" situation as [pushListItem]/[pushMediaRating]) - track first and re-resolve.
   */
  private suspend fun pushHidden(
    service: FloppyService,
    mediaType: String,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    var itemId = service.getMediaDetail(mediaType, item.source, item.mediaId).id
    if (itemId == null) {
      service.trackMedia(mediaType, FloppyTrackRequest(item.source, item.mediaId))
      itemId = service.getMediaDetail(mediaType, item.source, item.mediaId).id ?: return
    }
    val action = if (isAdd) "hide" else "unhide"
    service.toggleDiscoverHidden(FloppyDiscoverHiddenRequest(itemId, action))
  }

  /**
   * Adding an item to a list requires Floppy to already know about it - PUT returns
   * `404 "Media not found"` for anything it's never tracked or otherwise cataloged (confirmed
   * empirically; a plain metadata GET isn't enough to register it either) - so, same as
   * [pushMovieWatched], track it first on a 404 and retry.
   */
  private suspend fun pushListItem(
    service: FloppyService,
    mediaType: String,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    val listId = item.listId ?: return
    if (isAdd) {
      try {
        service.addToList(mediaType, item.source, item.mediaId, listId, FloppyEmptyRequest())
      } catch (error: Throwable) {
        service.trackMedia(mediaType, FloppyTrackRequest(item.source, item.mediaId))
        service.addToList(mediaType, item.source, item.mediaId, listId, FloppyEmptyRequest())
      }
    } else {
      service.removeFromList(mediaType, item.source, item.mediaId, listId)
    }
  }

  private suspend fun pushListDelete(
    service: FloppyService,
    item: FloppySyncQueue,
  ) {
    val listId = item.listId ?: return
    service.deleteList(listId)
  }

  /**
   * Show/movie ratings live on the same PATCH endpoint used for watched status, but unlike that
   * endpoint's `status` field, its `score` field rejects `null` on this Floppy version even though
   * the schema marks it nullable (confirmed empirically) - so clearing a rating here is a no-op on
   * Floppy's side; only the local rating is removed.
   * TODO: Revisit if a future Floppy version fixes this.
   *
   * Same "not tracked yet" 404 as [pushListItem]/[pushMovieWatched] applies here too - track first
   * and retry on failure.
   */
  private suspend fun pushMediaRating(
    service: FloppyService,
    mediaType: String,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    if (!isAdd) return
    try {
      service.updateMediaScore(mediaType, item.source, item.mediaId, FloppyScoreUpdateRequest(item.value))
    } catch (error: Throwable) {
      service.trackMedia(mediaType, FloppyTrackRequest(item.source, item.mediaId))
      service.updateMediaScore(mediaType, item.source, item.mediaId, FloppyScoreUpdateRequest(item.value))
    }
  }

  private suspend fun pushSeasonRating(
    service: FloppyService,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    if (!isAdd) return
    val seasonNumber = item.seasonNumber ?: return
    service.updateSeasonScore(item.source, item.mediaId, seasonNumber, FloppyScoreUpdateRequest(item.value))
  }

  private suspend fun pushEpisodeRating(
    service: FloppyService,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    val seasonNumber = item.seasonNumber ?: return
    val episodeNumber = item.episodeNumber ?: return
    val score = if (isAdd) item.value else null
    service.updateEpisodeScore(item.source, item.mediaId, seasonNumber, episodeNumber, FloppyScoreUpdateRequest(score))
  }

  private suspend fun pushWatchlist(
    service: FloppyService,
    mediaType: String,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    if (isAdd) {
      service.trackMedia(mediaType, FloppyTrackRequest(item.source, item.mediaId))
    } else {
      service.untrackMedia(mediaType, item.source, item.mediaId)
    }
  }

  private suspend fun pushMovieWatched(
    service: FloppyService,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    val status = if (isAdd) "completed" else "planning"
    try {
      service.updateStatus(MEDIA_TYPE_MOVIE, item.source, item.mediaId, FloppyStatusUpdateRequest(status))
    } catch (error: Throwable) {
      // Movie likely isn't tracked yet - PATCH has nothing to update. Track it first, then retry.
      if (!isAdd) throw error
      service.trackMedia(MEDIA_TYPE_MOVIE, FloppyTrackRequest(item.source, item.mediaId))
      service.updateStatus(MEDIA_TYPE_MOVIE, item.source, item.mediaId, FloppyStatusUpdateRequest(status))
    }
  }

  private suspend fun pushEpisodeWatched(
    service: FloppyService,
    item: FloppySyncQueue,
    isAdd: Boolean,
  ) {
    val seasonNumber = item.seasonNumber ?: return
    val episodeNumber = item.episodeNumber ?: return
    if (isAdd) {
      service.watchEpisode(item.source, item.mediaId, seasonNumber, episodeNumber)
    } else {
      service.unwatchEpisode(item.source, item.mediaId, seasonNumber, episodeNumber)
    }
  }
}
