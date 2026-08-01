package com.michaldrabik.ui_base.floppy

import androidx.work.WorkManager
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queues local watchlist/watched changes for push to Floppy, mirroring
 * [com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager]'s local-first + async-queue design.
 * Floppy media is addressed by tmdb id; idTmdb <= 0 means no tmdb mapping exists locally, so
 * there's nothing to address the item by on Floppy's side and it's skipped.
 */
@Singleton
class FloppySyncManager @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val workManager: WorkManager,
) {

  suspend fun scheduleShowWatchlist(
    idTmdb: Long,
    operation: Operation,
  ) {
    if (!isEnabled(idTmdb)) return
    enqueue(FloppySyncQueue.createShowWatchlist(idTmdb, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatchlist(
    idTmdb: Long,
    operation: Operation,
  ) {
    if (!isEnabled(idTmdb)) return
    enqueue(FloppySyncQueue.createMovieWatchlist(idTmdb, operation, nowUtcMillis()))
  }

  suspend fun scheduleMovieWatched(
    idTmdb: Long,
    operation: Operation,
  ) {
    if (!isEnabled(idTmdb)) return
    enqueue(FloppySyncQueue.createMovieWatched(idTmdb, operation, nowUtcMillis()))
  }

  suspend fun scheduleEpisodeWatched(
    showIdTmdb: Long,
    seasonNumber: Int,
    episodeNumber: Int,
    operation: Operation,
  ) {
    if (!isEnabled(showIdTmdb)) return
    enqueue(FloppySyncQueue.createEpisodeWatched(showIdTmdb, seasonNumber, episodeNumber, operation, nowUtcMillis()))
  }

  private fun isEnabled(idTmdb: Long): Boolean {
    if (!connectionManager.isConfigured()) return false
    if (idTmdb <= 0) {
      Timber.d("No TMDB id available. Skipping Floppy sync.")
      return false
    }
    return true
  }

  private suspend fun enqueue(item: FloppySyncQueue) {
    localSource.floppySyncQueue.insert(listOf(item))
    FloppySyncWorker.schedule(workManager)
  }
}
