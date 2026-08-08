package io.github.furaibo9714.diskette.ui_my_shows.myshows.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class MyShowsRatingsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRatings(): Map<MediaId, UserRating?> =
    withContext(dispatchers.IO) {
      ratingsRepository.shows.loadShowsRatings().associateBy { it.mediaId }
    }
}
