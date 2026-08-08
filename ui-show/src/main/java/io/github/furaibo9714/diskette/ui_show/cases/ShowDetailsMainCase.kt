package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      showsRepository.detailsShow.load(mediaId)
    }

  suspend fun removeMalformedShow(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      with(showsRepository) {
        myShows.delete(mediaId)
        watchlistShows.delete(mediaId)
        hiddenShows.delete(mediaId)
        detailsShow.delete(mediaId)
      }
      Timber.d("Removing malformed show...")
    }
}
