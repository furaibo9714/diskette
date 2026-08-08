package io.github.furaibo9714.diskette.ui_movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsHiddenCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun isHidden(movie: Movie) =
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.exists(movie.ids.media)
    }

  suspend fun addToHidden(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.insert(movie.ids.media)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieHidden(movie.ids, Operation.ADD)
    }
  }

  suspend fun removeFromHidden(movie: Movie) {
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.delete(movie.ids.media)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieHidden(movie.ids, Operation.REMOVE)
    }
  }
}
