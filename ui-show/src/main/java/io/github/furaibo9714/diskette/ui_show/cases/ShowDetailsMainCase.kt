package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    withContext(dispatchers.IO) {
      showsRepository.detailsShow.load(idTrakt)
    }

  suspend fun removeMalformedShow(idTrakt: IdTrakt) =
    withContext(dispatchers.IO) {
      with(showsRepository) {
        myShows.delete(idTrakt)
        watchlistShows.delete(idTrakt)
        hiddenShows.delete(idTrakt)
        detailsShow.delete(idTrakt)
      }
      Timber.d("Removing malformed show...")
    }
}
