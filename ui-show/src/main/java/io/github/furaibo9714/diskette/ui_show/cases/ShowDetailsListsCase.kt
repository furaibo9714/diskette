package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsListsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val listsRepository: ListsRepository,
) {

  suspend fun getListsCount(show: Show) =
    withContext(dispatchers.IO) {
      listsRepository.loadListIdsForItem(MediaId.parse(show.mediaId), Mode.SHOWS.type).size
    }
}
