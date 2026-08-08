package io.github.furaibo9714.diskette.ui_base.floppy

import androidx.work.WorkManager
import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.SOURCE_MANUAL
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.SOURCE_TMDB
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queues local watchlist/watched changes for push to Floppy, mirroring
 * [io.github.furaibo9714.diskette.ui_base.trakt.quicksync.QuickSyncManager]'s local-first + async-queue design.
 * Floppy media is addressed by (source, media_id): TMDB-backed items use the real tmdb id;
 * Floppy "manual" items (imported via [io.github.furaibo9714.diskette.ui_base.floppy.imports.FloppyManualMediaResolver])
 * carry a synthetic id_trakt with the original Floppy UUID recovered from id_slug. Items with
 * neither have nothing to address on Floppy's side and are skipped.
 */
@Singleton
class FloppySyncManager @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val workManager: WorkManager,
) {

  suspend fun scheduleShowWatchlist(
    ids: Ids,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    enqueue(FloppySyncQueue.createShowWatchlist(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatchlist(
    ids: Ids,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    enqueue(FloppySyncQueue.createMovieWatchlist(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatched(
    ids: Ids,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    enqueue(FloppySyncQueue.createMovieWatched(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleEpisodeWatched(
    showIds: Ids,
    seasonNumber: Int,
    episodeNumber: Int,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showIds) ?: return
    enqueue(FloppySyncQueue.createEpisodeWatched(source, mediaId, seasonNumber, episodeNumber, operation, nowUtcMillis()))
  }

  suspend fun scheduleShowRating(
    ids: Ids,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createShowRating(source, mediaId, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleMovieRating(
    ids: Ids,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createMovieRating(source, mediaId, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleSeasonRating(
    showIds: Ids,
    seasonNumber: Int,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showIds) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createSeasonRating(source, mediaId, seasonNumber, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleEpisodeRating(
    showIds: Ids,
    seasonNumber: Int,
    episodeNumber: Int,
    score: Int?,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(showIds) ?: return
    val operation = if (score != null) Operation.ADD else Operation.REMOVE
    enqueue(FloppySyncQueue.createEpisodeRating(source, mediaId, seasonNumber, episodeNumber, operation, score, nowUtcMillis()))
  }

  suspend fun scheduleListItemAdd(
    ids: Ids,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    if (listFloppyId == null) return
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    val item = when (mode) {
      Mode.SHOWS -> FloppySyncQueue.createListItemShow(source, mediaId, listFloppyId, Operation.ADD, nowUtcMillis())
      Mode.MOVIES -> FloppySyncQueue.createListItemMovie(source, mediaId, listFloppyId, Operation.ADD, nowUtcMillis())
    }
    enqueue(item)
  }

  suspend fun scheduleListItemRemove(
    ids: Ids,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    if (listFloppyId == null) return
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    val item = when (mode) {
      Mode.SHOWS -> FloppySyncQueue.createListItemShow(source, mediaId, listFloppyId, Operation.REMOVE, nowUtcMillis())
      Mode.MOVIES -> FloppySyncQueue.createListItemMovie(source, mediaId, listFloppyId, Operation.REMOVE, nowUtcMillis())
    }
    enqueue(item)
  }

  suspend fun scheduleListItemAdd(
    itemId: MediaId,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    val ids = when (mode) {
      Mode.SHOWS -> resolveShowIds(itemId)
      Mode.MOVIES -> resolveMovieIds(itemId)
    } ?: return
    scheduleListItemAdd(ids, mode, listFloppyId)
  }

  suspend fun scheduleListItemRemove(
    itemId: MediaId,
    mode: Mode,
    listFloppyId: Long?,
  ) {
    val ids = when (mode) {
      Mode.SHOWS -> resolveShowIds(itemId)
      Mode.MOVIES -> resolveMovieIds(itemId)
    } ?: return
    scheduleListItemRemove(ids, mode, listFloppyId)
  }

  suspend fun scheduleListDelete(listFloppyId: Long?) {
    if (listFloppyId == null || !connectionManager.isConfigured()) return
    enqueue(FloppySyncQueue.createListDelete(listFloppyId, nowUtcMillis()))
  }

  suspend fun scheduleShowHidden(
    ids: Ids,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    enqueue(FloppySyncQueue.createShowHidden(source, mediaId, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieHidden(
    ids: Ids,
    operation: Operation,
  ) {
    val (source, mediaId) = resolveSourceAndMediaId(ids) ?: return
    enqueue(FloppySyncQueue.createMovieHidden(source, mediaId, operation, nowUtcMillis()))
  }

  /**
   * Convenience overloads for call sites that only have an [MediaId] on hand (context-menu
   * sheets, widgets, progress-tab quick actions) rather than the full [Ids] a details screen
   * already has in memory. Resolves the local row's tmdb id/id_slug before delegating to the
   * [Ids]-based overload above; if the item isn't cached locally there's nothing to resolve and
   * the sync is silently skipped, same as any other unresolvable case.
   */
  suspend fun scheduleShowWatchlist(
    showId: MediaId,
    operation: Operation,
  ) {
    val ids = resolveShowIds(showId) ?: return
    scheduleShowWatchlist(ids, operation)
  }

  suspend fun scheduleMovieWatchlist(
    movieId: MediaId,
    operation: Operation,
  ) {
    val ids = resolveMovieIds(movieId) ?: return
    scheduleMovieWatchlist(ids, operation)
  }

  suspend fun scheduleMovieWatched(
    movieId: MediaId,
    operation: Operation,
  ) {
    val ids = resolveMovieIds(movieId) ?: return
    scheduleMovieWatched(ids, operation)
  }

  suspend fun scheduleEpisodeWatched(
    showId: MediaId,
    seasonNumber: Int,
    episodeNumber: Int,
    operation: Operation,
  ) {
    val ids = resolveShowIds(showId) ?: return
    scheduleEpisodeWatched(ids, seasonNumber, episodeNumber, operation)
  }

  suspend fun scheduleShowHidden(
    showId: MediaId,
    operation: Operation,
  ) {
    val ids = resolveShowIds(showId) ?: return
    scheduleShowHidden(ids, operation)
  }

  suspend fun scheduleMovieHidden(
    movieId: MediaId,
    operation: Operation,
  ) {
    val ids = resolveMovieIds(movieId) ?: return
    scheduleMovieHidden(ids, operation)
  }

  suspend fun scheduleShowRating(
    showId: MediaId,
    score: Int?,
  ) {
    val ids = resolveShowIds(showId) ?: return
    scheduleShowRating(ids, score)
  }

  suspend fun scheduleMovieRating(
    movieId: MediaId,
    score: Int?,
  ) {
    val ids = resolveMovieIds(movieId) ?: return
    scheduleMovieRating(ids, score)
  }

  /**
   * [seasonId] is the season's own id_trakt (what local rating storage keys off), not the parent
   * show's - resolved to the show via the local `Season.showMediaId` column since Floppy addresses
   * seasons through the show's media id, not a season-level one.
   */
  suspend fun scheduleSeasonRating(
    seasonId: MediaId,
    seasonNumber: Int,
    score: Int?,
  ) {
    val season = localSource.seasons.getById(seasonId.id) ?: return
    val showIds = resolveShowIds(MediaId.parse(season.showMediaId)) ?: return
    scheduleSeasonRating(showIds, seasonNumber, score)
  }

  /**
   * [episodeId] is the episode's own id_trakt, resolved to the parent show the same way as
   * [scheduleSeasonRating] above.
   */
  suspend fun scheduleEpisodeRating(
    episodeId: MediaId,
    seasonNumber: Int,
    episodeNumber: Int,
    score: Int?,
  ) {
    val episode = localSource.episodes.getAll(listOf(episodeId.id)).firstOrNull() ?: return
    val showIds = resolveShowIds(MediaId.parse(episode.showMediaId)) ?: return
    scheduleEpisodeRating(showIds, seasonNumber, episodeNumber, score)
  }

  private suspend fun resolveShowIds(showId: MediaId): Ids? {
    val show = localSource.shows.getById(showId.id) ?: return null
    return Ids.EMPTY.copy(trakt = showId, tmdb = IdTmdb(show.idTmdb), slug = IdSlug(show.idSlug))
  }

  private suspend fun resolveMovieIds(movieId: MediaId): Ids? {
    val movie = localSource.movies.getById(movieId.id) ?: return null
    return Ids.EMPTY.copy(trakt = movieId, tmdb = IdTmdb(movie.idTmdb), slug = IdSlug(movie.idSlug))
  }

  /**
   * TMDB-backed items resolve to (tmdb, realTmdbId). Floppy manual imports carry a synthetic
   * id_trakt (see [FloppyManualSyntheticIds]) with the original Floppy UUID stashed in id_slug -
   * resolve those to (manual, uuid). Anything else has no Floppy-side address and is skipped.
   */
  private fun resolveSourceAndMediaId(ids: Ids): Pair<String, String>? {
    if (!connectionManager.isConfigured()) return null

    if (FloppyManualSyntheticIds.isSynthetic(ids.media.key)) {
      val floppyMediaId = FloppyManualSyntheticIds.extractFloppyMediaId(ids.slug.id)
      if (floppyMediaId == null) {
        Timber.d("Synthetic manual id with no recoverable Floppy UUID. Skipping Floppy sync.")
        return null
      }
      return SOURCE_MANUAL to floppyMediaId
    }

    if (ids.tmdb.id > 0) return SOURCE_TMDB to ids.tmdb.id.toString()

    Timber.d("No TMDB id available. Skipping Floppy sync.")
    return null
  }

  private suspend fun enqueue(item: FloppySyncQueue) {
    localSource.floppySyncQueue.insert(listOf(item))
    FloppySyncWorker.schedule(workManager)
  }
}
