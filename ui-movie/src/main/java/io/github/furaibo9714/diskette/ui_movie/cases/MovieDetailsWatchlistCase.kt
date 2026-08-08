package io.github.furaibo9714.diskette.ui_movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsWatchlistCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isWatchlist(movie: Movie) =
    withContext(dispatchers.IO) {
      moviesRepository.watchlistMovies.load(movie.ids.media) != null
    }

  suspend fun addToWatchlist(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.watchlistMovies.insert(movie.ids.media)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieWatchlist(movie.ids, Operation.ADD)
      announcementManager.refreshMoviesAnnouncements()
    }
  }

  suspend fun removeFromWatchlist(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.watchlistMovies.delete(movie.ids.media)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieWatchlist(movie.ids, Operation.REMOVE)
    }
  }
}
