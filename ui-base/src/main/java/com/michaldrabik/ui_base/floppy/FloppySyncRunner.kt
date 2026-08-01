package com.michaldrabik.ui_base.floppy

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Type
import com.michaldrabik.data_remote.floppy.api.FloppyService
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

  suspend fun run(): Int {
    if (!connectionManager.isConfigured()) return 0

    val items = localSource.floppySyncQueue.getAll()
    if (items.isEmpty()) return 0

    val service = connectionManager.service()
    var pushed = 0

    items.forEach { item ->
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
    }
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
