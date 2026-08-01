package com.michaldrabik.ui_base.common.sheets.ratings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_base.floppy.FloppySyncManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class RatingsSeasonCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating =
    withContext(dispatchers.IO) {
      val season = Season.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
      val rating = ratingsRepository.shows.loadRatingsSeasons(listOf(season))
      rating.firstOrNull() ?: TraktRating.EMPTY
    }

  suspend fun saveRating(
    idTrakt: IdTrakt,
    rating: Int,
    seasonNumber: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val season = Season.EMPTY.copy(
      ids = Ids.EMPTY.copy(trakt = idTrakt),
      number = seasonNumber,
    )

    ratingsRepository.shows.addRating(season = season, rating = rating)
    floppySyncManager.scheduleSeasonRating(idTrakt, seasonNumber, rating)
  }

  suspend fun deleteRating(
    idTrakt: IdTrakt,
    seasonNumber: Int,
  ) = withContext(dispatchers.IO) {
    val season = Season.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    ratingsRepository.shows.deleteRating(season = season)
    floppySyncManager.scheduleSeasonRating(idTrakt, seasonNumber, null)
  }
}
