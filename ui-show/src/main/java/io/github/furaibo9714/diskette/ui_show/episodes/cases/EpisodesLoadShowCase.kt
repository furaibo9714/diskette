package io.github.furaibo9714.diskette.ui_show.episodes.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesLoadShowCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      showsRepository.detailsShow.load(mediaId)
    }
}
