package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class RatingsSeasonCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(mediaId: MediaId): UserRating =
    withContext(dispatchers.IO) {
      val season = Season.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = mediaId))
      val rating = ratingsRepository.shows.loadRatingsSeasons(listOf(season))
      rating.firstOrNull() ?: UserRating.EMPTY
    }

  suspend fun saveRating(
    mediaId: MediaId,
    rating: Int,
    seasonNumber: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val season = Season.EMPTY.copy(
      ids = Ids.EMPTY.copy(trakt = mediaId),
      number = seasonNumber,
    )

    ratingsRepository.shows.addRating(season = season, rating = rating)
    floppySyncManager.scheduleSeasonRating(mediaId, seasonNumber, rating)
  }

  suspend fun deleteRating(
    mediaId: MediaId,
    seasonNumber: Int,
  ) = withContext(dispatchers.IO) {
    val season = Season.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = mediaId))
    ratingsRepository.shows.deleteRating(season = season)
    floppySyncManager.scheduleSeasonRating(mediaId, seasonNumber, null)
  }
}
