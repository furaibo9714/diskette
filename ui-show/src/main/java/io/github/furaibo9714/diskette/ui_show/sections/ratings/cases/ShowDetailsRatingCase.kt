package io.github.furaibo9714.diskette.ui_show.sections.ratings.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRatingCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(show: Show) =
    withContext(dispatchers.IO) {
      ratingsRepository.shows.loadRatings(listOf(show)).firstOrNull()
    }

  suspend fun loadExternalRatings(show: Show) =
    withContext(dispatchers.IO) {
      ratingsRepository.shows.external.loadRatings(show)
    }
}
