package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class RatingsShowCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating =
    withContext(dispatchers.IO) {
      val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
      val rating = ratingsRepository.shows.loadRatings(listOf(show))
      rating.firstOrNull() ?: TraktRating.EMPTY
    }

  suspend fun saveRating(
    idTrakt: IdTrakt,
    rating: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    ratingsRepository.shows.addRating(show = show, rating = rating)
    floppySyncManager.scheduleShowRating(idTrakt, rating)
  }

  suspend fun deleteRating(idTrakt: IdTrakt) =
    withContext(dispatchers.IO) {
      val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
      ratingsRepository.shows.deleteRating(show = show)
      floppySyncManager.scheduleShowRating(idTrakt, null)
    }
}
