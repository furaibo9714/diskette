package com.michaldrabik.ui_base.sync.runners

import com.michaldrabik.common.ConfigVariant.SHOW_SYNC_COOLDOWN
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.EpisodesSyncLog
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.ShowStatus.CANCELED
import com.michaldrabik.ui_model.ShowStatus.ENDED
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is responsible for fetching and syncing missing/updated episodes data for current progress shows.
 */
@Singleton
class ShowsSyncRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val episodesManager: EpisodesManager,
  private val showsRepository: ShowsRepository,
) {

  companion object {
    private const val DELAY_MS = 100L

    /**
     * A Floppy full-library import can leave hundreds of shows never-synced (UNKNOWN status,
     * see below) all at once. Backfilling every one of them - full show details + a full
     * season/episode delete-and-reinsert each - in a single run has been observed to run the
     * heap out of memory on large libraries (900+ shows queued in one run). Capping bounds each
     * run's peak memory use; the rest gets picked up by the next run, since a full sync already
     * fires on every app start and Progress pull-to-refresh.
     */
    private const val MAX_SHOWS_PER_RUN = 25
  }

  suspend fun run(): Int {
    Timber.i("Shows sync initialized.")

    val myShows = showsRepository.myShows.loadAll()
    val watchlistShows = showsRepository.watchlistShows.loadAll()
    val watchlistShowsIds = watchlistShows.map { it.traktId }
    val syncLog = localSource.episodesSyncLog.getAll()

    fun lastSyncOf(traktId: Long) = syncLog.find { it.idTrakt == traktId }?.syncedAt ?: 0

    /**
     * UNKNOWN is deliberately not excluded here: it's what a Floppy-imported thin show row reads
     * as before its first real detail fetch, not a confirmed-stable status like ENDED/CANCELED -
     * excluding it would permanently skip syncing any freshly imported show's episode data.
     */
    val showsToSync = (myShows + watchlistShows)
      .filter { it.status !in arrayOf(ENDED, CANCELED) }
      .filter { nowUtcMillis() - lastSyncOf(it.traktId) >= SHOW_SYNC_COOLDOWN }
      .sortedBy { lastSyncOf(it.traktId) } // never-synced (0) and longest-stale shows first
      .take(MAX_SHOWS_PER_RUN)

    Timber.i("Shows to sync: ${showsToSync.size}.")
    if (showsToSync.isEmpty()) {
      Timber.i("Nothing to sync. Stopping...")
      return 0
    }

    var syncCount = 0
    showsToSync.forEach { show ->
      val isInWatchlist = show.traktId in watchlistShowsIds

      try {
        Timber.i("Syncing ${show.title}(${show.ids.trakt}) details...")
        showsRepository.detailsShow.load(show.ids.trakt, force = true)
        syncCount++
        Timber.i("${show.title}(${show.ids.trakt}) show synced.")
      } catch (t: Throwable) {
        Timber.e("${show.title}(${show.ids.trakt}) show sync error. Skipping... \n$t")
      }

      if (isInWatchlist) {
        localSource.episodesSyncLog.upsert(EpisodesSyncLog(show.traktId, nowUtcMillis()))
      } else {
        try {
          Timber.i("Syncing ${show.title}(${show.ids.trakt}) episodes...")

          val remoteSeasons = remoteSource.trakt
            .fetchSeasons(show.traktId)
            .map { mappers.season.fromNetwork(it) }
          episodesManager.invalidateSeasons(show, remoteSeasons)
          syncCount++

          Timber.i("${show.title}(${show.ids.trakt}) episodes synced.")
        } catch (t: Throwable) {
          Timber.e("${show.title}(${show.ids.trakt}) episodes sync error. Skipping... \n$t")
        } finally {
          delay(DELAY_MS)
        }
      }
    }

    return syncCount
  }
}
