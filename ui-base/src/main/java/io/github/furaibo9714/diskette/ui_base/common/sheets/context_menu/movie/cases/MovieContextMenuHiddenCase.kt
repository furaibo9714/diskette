package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuHiddenCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun moveToHidden(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))

      moviesRepository.hiddenMovies.insert(movie.ids.media)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieHidden(mediaId, Operation.ADD)
    }

  suspend fun removeFromHidden(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      moviesRepository.hiddenMovies.delete(mediaId)
      floppySyncManager.scheduleMovieHidden(mediaId, Operation.REMOVE)
    }
}
