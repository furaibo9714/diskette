package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class RatingsEpisodeCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(mediaId: MediaId): UserRating =
    withContext(dispatchers.IO) {
      val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = mediaId))
      val rating = ratingsRepository.shows.loadRating(episode)
      rating ?: UserRating.EMPTY
    }

  suspend fun saveRating(
    mediaId: MediaId,
    rating: Int,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val episode = Episode.EMPTY.copy(
      ids = Ids.EMPTY.copy(trakt = mediaId),
      season = seasonNumber,
      number = episodeNumber,
    )

    ratingsRepository.shows.addRating(episode = episode, rating = rating)
    floppySyncManager.scheduleEpisodeRating(mediaId, seasonNumber, episodeNumber, rating)
  }

  suspend fun deleteRating(
    mediaId: MediaId,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = withContext(dispatchers.IO) {
    val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = mediaId))
    ratingsRepository.shows.deleteRating(episode = episode)
    floppySyncManager.scheduleEpisodeRating(mediaId, seasonNumber, episodeNumber, null)
  }
}
