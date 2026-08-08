package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class RatingsMovieCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(mediaId: MediaId): UserRating =
    withContext(dispatchers.IO) {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(media = mediaId))
      val rating = ratingsRepository.movies.loadRatings(listOf(movie))
      rating.firstOrNull() ?: UserRating.EMPTY
    }

  suspend fun saveRating(
    mediaId: MediaId,
    rating: Int,
  ) = withContext(dispatchers.IO) {
    check(rating in RATING_VALID_RANGE)

    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(media = mediaId))
    ratingsRepository.movies.addRating(movie = movie, rating = rating)
    floppySyncManager.scheduleMovieRating(mediaId, rating)
  }

  suspend fun deleteRating(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(media = mediaId))
      ratingsRepository.movies.deleteRating(movie = movie)
      floppySyncManager.scheduleMovieRating(mediaId, null)
    }
}
