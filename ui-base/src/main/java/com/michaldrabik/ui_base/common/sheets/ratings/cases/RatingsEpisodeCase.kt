package com.michaldrabik.ui_base.common.sheets.ratings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_base.floppy.FloppySyncManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class RatingsEpisodeCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating =
    withContext(dispatchers.IO) {
      val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
      val rating = ratingsRepository.shows.loadRating(episode)
      rating ?: TraktRating.EMPTY
    }

  suspend fun saveRating(
    idTrakt: IdTrakt,
    rating: Int,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val episode = Episode.EMPTY.copy(
      ids = Ids.EMPTY.copy(trakt = idTrakt),
      season = seasonNumber,
      number = episodeNumber,
    )

    ratingsRepository.shows.addRating(episode = episode, rating = rating)
    floppySyncManager.scheduleEpisodeRating(idTrakt, seasonNumber, episodeNumber, rating)
  }

  suspend fun deleteRating(
    idTrakt: IdTrakt,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = withContext(dispatchers.IO) {
    val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    ratingsRepository.shows.deleteRating(episode = episode)
    floppySyncManager.scheduleEpisodeRating(idTrakt, seasonNumber, episodeNumber, null)
  }
}
