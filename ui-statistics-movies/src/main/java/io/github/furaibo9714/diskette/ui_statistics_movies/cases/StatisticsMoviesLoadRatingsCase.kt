package io.github.furaibo9714.diskette.ui_statistics_movies.cases

import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class StatisticsMoviesLoadRatingsCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val ratingsRepository: RatingsRepository,
  private val imagesProvider: MovieImagesProvider,
) {

  companion object {
    private const val LIMIT = 25
  }

  suspend fun loadRatings(): List<StatisticsMoviesRatingItem> {
    val ratings = ratingsRepository.movies.loadMoviesRatings()
    val ratingsIds = ratings.map { it.mediaId }
    val myMovies = moviesRepository.myMovies.loadAll(ratingsIds).distinctBy { it.mediaId }

    return ratings
      .filter { rating -> myMovies.any { it.mediaId == rating.mediaId.id } }
      .take(LIMIT)
      .map { rating ->
        val movie = myMovies.first { it.mediaId == rating.mediaId.id }
        StatisticsMoviesRatingItem(
          isLoading = false,
          movie = movie,
          image = imagesProvider.findCachedImage(movie, ImageType.POSTER),
          rating = rating,
        )
      }.sortedByDescending { it.rating.ratedAt }
  }
}
