package io.github.furaibo9714.diskette.ui_movie.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsListsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val listsRepository: ListsRepository,
) {

  suspend fun countLists(movie: Movie) =
    withContext(dispatchers.IO) {
      listsRepository.loadListIdsForItem(movie.mediaId, Mode.MOVIES.type).size
    }
}
