package io.github.furaibo9714.diskette.ui_base.sync.runners

import io.github.furaibo9714.diskette.common.ConfigVariant.SHOW_SYNC_COOLDOWN
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.EpisodesSyncLog
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.ShowStatus.CANCELED
import io.github.furaibo9714.diskette.ui_model.ShowStatus.ENDED
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import timber.log.Timber

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
  }

  var progressListener: (suspend (count: Int, total: Int) -> Unit)? = null

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

    Timber.i("Shows to sync: ${showsToSync.size}.")
    if (showsToSync.isEmpty()) {
      Timber.i("Nothing to sync. Stopping...")
      return 0
    }

    var syncCount = 0
    showsToSync.forEachIndexed { index, show ->
      progressListener?.invoke(index + 1, showsToSync.size)
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

          val remoteSeasons = remoteSource.media
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
