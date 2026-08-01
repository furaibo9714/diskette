package com.michaldrabik.ui_base.floppy.imports

import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import com.michaldrabik.data_remote.floppy.api.FloppyService
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.repository.movies.WatchlistMoviesRepository
import com.michaldrabik.repository.shows.WatchlistShowsRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls the user's tracked shows/movies from Floppy and reconciles Showly's local watchlist.
 * Items are matched by tmdb id where possible; Floppy "manual" items (no external provider id)
 * are resolved to a thin local Show/Movie row via [FloppyManualMediaResolver] instead of being
 * skipped. Floppy has no delta/"since" endpoint, so this is a full reconcile each run.
 */
@Singleton
class FloppyImportWatchlistRunner @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val mediaResolver: FloppyManualMediaResolver,
  private val watchlistShowsRepository: WatchlistShowsRepository,
  private val watchlistMoviesRepository: WatchlistMoviesRepository,
) {

  suspend fun run(): Int {
    if (!connectionManager.isConfigured()) return 0

    val service = connectionManager.service()
    var count = 0
    count += importShows(service)
    count += importMovies(service)
    return count
  }

  private suspend fun importShows(service: FloppyService): Int {
    var imported = 0
    var offset = 0
    while (true) {
      val page = service.getTrackedMedia(MEDIA_TYPE_TV, FloppyService.MEDIA_LIST_PAGE_SIZE, offset)
      page.results
        .filter { (it.status ?: -1) == FLOPPY_STATUS_PLANNING }
        .forEach { media ->
          val id = mediaResolver.resolveShowId(media.item) ?: return@forEach
          if (!watchlistShowsRepository.exists(id)) {
            watchlistShowsRepository.insert(id)
            imported++
          }
        }
      if (page.results.size < FloppyService.MEDIA_LIST_PAGE_SIZE) break
      offset += FloppyService.MEDIA_LIST_PAGE_SIZE
    }
    Timber.d("Imported $imported show(s) into watchlist from Floppy.")
    return imported
  }

  private suspend fun importMovies(service: FloppyService): Int {
    var imported = 0
    var offset = 0
    while (true) {
      val page = service.getTrackedMedia(MEDIA_TYPE_MOVIE, FloppyService.MEDIA_LIST_PAGE_SIZE, offset)
      page.results
        .filter { (it.status ?: -1) == FLOPPY_STATUS_PLANNING }
        .forEach { media ->
          val id = mediaResolver.resolveMovieId(media.item) ?: return@forEach
          if (!watchlistMoviesRepository.exists(id)) {
            watchlistMoviesRepository.insert(id)
            imported++
          }
        }
      if (page.results.size < FloppyService.MEDIA_LIST_PAGE_SIZE) break
      offset += FloppyService.MEDIA_LIST_PAGE_SIZE
    }
    Timber.d("Imported $imported movie(s) into watchlist from Floppy.")
    return imported
  }

  companion object {
    private const val FLOPPY_STATUS_PLANNING = 0
  }
}
