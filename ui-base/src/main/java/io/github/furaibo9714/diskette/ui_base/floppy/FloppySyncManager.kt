package io.github.furaibo9714.diskette.ui_base.floppy

import androidx.work.WorkManager
import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.MediaSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queues local watchlist/watched changes for push to Floppy, local-first with an async queue so a
 * missing or slow server never blocks the UI.
 *
 * Floppy addresses media by (source, media_id), which is exactly what a [MediaId] holds, so the
 * item the user acted on can be pushed without any lookup or translation. Screens that already
 * have a full [Ids] in memory can pass that instead.
 */
@Singleton
class FloppySyncManager @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val workManager: WorkManager,
) {

  suspend fun scheduleShowWatchlist(
    showId: MediaId,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showId) ?: return
    enqueue(FloppySyncQueue.createShowWatchlist(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatchlist(
    movieId: MediaId,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(movieId) ?: return
    enqueue(FloppySyncQueue.createMovieWatchlist(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatched(
    movieId: MediaId,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(movieId) ?: return
    enqueue(FloppySyncQueue.createMovieWatched(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleEpisodeWatched(
    showId: MediaId,
    seasonNumber: Int,
    episodeNumber: Int,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showId) ?: return
    enqueue(FloppySyncQueue.createEpisodeWatched(source, mediaId, seasonNumber, episodeNumber, operation, nowUtcMillis()))
  }

  suspend fun scheduleShowRating(
    showId: MediaId,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showId) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createShowRating(source, mediaId, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleMovieRating(
    movieId: MediaId,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(movieId) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createMovieRating(source, mediaId, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleShowHidden(
    showId: MediaId,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showId) ?: return
    enqueue(FloppySyncQueue.createShowHidden(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieHidden(
    movieId: MediaId,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(movieId) ?: return
    enqueue(FloppySyncQueue.createMovieHidden(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleListItemAdd(
    itemId: MediaId,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    if (listFloppyId == null) return
    val (source, mediaId) = resolveSourceAndMediaId(itemId) ?: return
    val item = when (mode) {
      Mode.SHOWS -> FloppySyncQueue.createListItemShow(source, mediaId, listFloppyId, Operation.ADD, nowUtcMillis())
      Mode.MOVIES -> FloppySyncQueue.createListItemMovie(source, mediaId, listFloppyId, Operation.ADD, nowUtcMillis())
    }
    enqueue(item)
  }

  suspend fun scheduleListItemRemove(
    itemId: MediaId,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    if (listFloppyId == null) return
    val (source, mediaId) = resolveSourceAndMediaId(itemId) ?: return
    val item = when (mode) {
      Mode.SHOWS -> FloppySyncQueue.createListItemShow(source, mediaId, listFloppyId, Operation.REMOVE, nowUtcMillis())
      Mode.MOVIES -> FloppySyncQueue.createListItemMovie(source, mediaId, listFloppyId, Operation.REMOVE, nowUtcMillis())
    }
    enqueue(item)
  }

  suspend fun scheduleListDelete(listFloppyId: Long?) {
    if (listFloppyId == null || !connectionManager.isConfigured()) return
    enqueue(FloppySyncQueue.createListDelete(listFloppyId, nowUtcMillis()))
  }

  /**
   * [seasonId] is the season's own id (what local rating storage keys off), not the parent show's,
   * so it is resolved to the show through the local `Season.showMediaId` column - Floppy addresses
   * seasons through the show's media id rather than a season-level one.
   */
  suspend fun scheduleSeasonRating(
    seasonId: MediaId,
    seasonNumber: Int,
    score: Int?,
  ) {
    val season = localSource.seasons.getById(seasonId.key) ?: return
    val (source, mediaId) = resolveSourceAndMediaId(MediaId.parse(season.showMediaId)) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createSeasonRating(source, mediaId, seasonNumber, operation, score, nowUtcMillis()))
  }

  /**
   * [episodeId] is the episode's own id, resolved to the parent show the same way as
   * [scheduleSeasonRating] above.
   */
  suspend fun scheduleEpisodeRating(
    episodeId: MediaId,
    seasonNumber: Int,
    episodeNumber: Int,
    score: Int?,
  ) {
    val episode = localSource.episodes.getAll(listOf(episodeId.key)).firstOrNull() ?: return
    val (source, mediaId) = resolveSourceAndMediaId(MediaId.parse(episode.showMediaId)) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createEpisodeRating(source, mediaId, seasonNumber, episodeNumber, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleShowWatchlist(
    ids: Ids,
    operation: Operation,
  ) = scheduleShowWatchlist(ids.media, operation)

  suspend fun scheduleMovieWatchlist(
    ids: Ids,
    operation: Operation,
  ) = scheduleMovieWatchlist(ids.media, operation)

  suspend fun scheduleMovieWatched(
    ids: Ids,
    operation: Operation,
  ) = scheduleMovieWatched(ids.media, operation)

  suspend fun scheduleEpisodeWatched(
    showIds: Ids,
    seasonNumber: Int,
    episodeNumber: Int,
    operation: Operation,
  ) = scheduleEpisodeWatched(showIds.media, seasonNumber, episodeNumber, operation)

  suspend fun scheduleShowRating(
    ids: Ids,
    score: Int?,
  ) = scheduleShowRating(ids.media, score)

  suspend fun scheduleMovieRating(
    ids: Ids,
    score: Int?,
  ) = scheduleMovieRating(ids.media, score)

  suspend fun scheduleShowHidden(
    ids: Ids,
    operation: Operation,
  ) = scheduleShowHidden(ids.media, operation)

  suspend fun scheduleMovieHidden(
    ids: Ids,
    operation: Operation,
  ) = scheduleMovieHidden(ids.media, operation)

  suspend fun scheduleListItemAdd(
    ids: Ids,
    mode: Mode,
    listFloppyId: Long?,
  ) = scheduleListItemAdd(ids.media, mode, listFloppyId)

  suspend fun scheduleListItemRemove(
    ids: Ids,
    mode: Mode,
    listFloppyId: Long?,
  ) = scheduleListItemRemove(ids.media, mode, listFloppyId)

  /**
   * A [MediaId] is already the (source, media_id) pair Floppy addresses media by, so there is
   * nothing to translate. Only an item Floppy can't name - one with no id, or one the app named
   * itself - has nowhere to sync to.
   */
  private fun resolveSourceAndMediaId(mediaId: MediaId): Pair<String, String>? {
    if (!connectionManager.isConfigured()) return null
    if (mediaId.isEmpty || mediaId.source == MediaSource.LOCAL) {
      Timber.d("Item has no Floppy-addressable id ($mediaId). Skipping Floppy sync.")
      return null
    }
    return mediaId.source.key to mediaId.providerId
  }

  private suspend fun enqueue(item: FloppySyncQueue) {
    localSource.floppySyncQueue.insert(listOf(item))
    FloppySyncWorker.schedule(workManager)
  }
}
