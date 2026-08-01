package com.michaldrabik.ui_base.floppy

import androidx.work.WorkManager
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.SOURCE_MANUAL
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.SOURCE_TMDB
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.ui_model.Ids
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queues local watchlist/watched changes for push to Floppy, mirroring
 * [com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager]'s local-first + async-queue design.
 * Floppy media is addressed by (source, media_id): TMDB-backed items use the real tmdb id;
 * Floppy "manual" items (imported via [com.michaldrabik.ui_base.floppy.imports.FloppyManualMediaResolver])
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

  /**
   * TMDB-backed items resolve to (tmdb, realTmdbId). Floppy manual imports carry a synthetic
   * id_trakt (see [FloppyManualSyntheticIds]) with the original Floppy UUID stashed in id_slug -
   * resolve those to (manual, uuid). Anything else has no Floppy-side address and is skipped.
   */
  private fun resolveSourceAndMediaId(ids: Ids): Pair<String, String>? {
    if (!connectionManager.isConfigured()) return null

    if (FloppyManualSyntheticIds.isSynthetic(ids.trakt.id)) {
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
