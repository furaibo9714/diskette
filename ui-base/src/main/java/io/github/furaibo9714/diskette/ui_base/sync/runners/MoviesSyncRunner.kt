package io.github.furaibo9714.diskette.ui_base.sync.runners

import io.github.furaibo9714.diskette.common.ConfigVariant.MOVIE_SYNC_COOLDOWN
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_model.MovieStatus.IN_PRODUCTION
import io.github.furaibo9714.diskette.ui_model.MovieStatus.PLANNED
import io.github.furaibo9714.diskette.ui_model.MovieStatus.POST_PRODUCTION
import io.github.furaibo9714.diskette.ui_model.MovieStatus.RUMORED
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is responsible for fetching and syncing missing/updated movies data.
 */
@Singleton
class MoviesSyncRunner @Inject constructor(
  private val localSource: LocalDataSource,
  private val moviesRepository: MoviesRepository,
  private val settingsRepository: SettingsRepository,
) {

  companion object {
    private const val DELAY_MS = 10L
  }

  suspend fun run(): Int {
    Timber.i("Movies sync initialized.")

    if (!settingsRepository.isMoviesEnabled) {
      Timber.i("Movies are disabled. Exiting...")
      return 0
    }

    val movies = moviesRepository.loadCollection()

    val moviesToSync = movies.filter { it.status in arrayOf(PLANNED, IN_PRODUCTION, POST_PRODUCTION, RUMORED) }
    if (moviesToSync.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return 0
    }
    Timber.i("Movies to sync count: ${moviesToSync.size}.")

    var syncCount = 0
    val syncLog = localSource.moviesSyncLog.getAll()
    moviesToSync.forEach { movie ->
      val lastSync = syncLog.find { it.mediaId == movie.ids.media.id }?.syncedAt ?: 0
      if (nowUtcMillis() - lastSync < MOVIE_SYNC_COOLDOWN) {
        Timber.i("${movie.title} is on cooldown. No need to sync.")
        return@forEach
      }

      try {
        Timber.i("Syncing ${movie.title}(${movie.ids.media}) details...")
        moviesRepository.movieDetails.load(movie.ids.media, force = true)
        syncCount++
        Timber.i("${movie.title}(${movie.ids.media}) movie synced.")
      } catch (t: Throwable) {
        Timber.e("${movie.title}(${movie.ids.media}) movie sync error. Skipping... \n$t")
      } finally {
        delay(DELAY_MS)
      }
    }

    return syncCount
  }
}
