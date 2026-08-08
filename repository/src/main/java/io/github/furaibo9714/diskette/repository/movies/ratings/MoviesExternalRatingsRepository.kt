package io.github.furaibo9714.diskette.repository.movies.ratings

import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Ratings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesExternalRatingsRepository @Inject constructor(
  private val mappers: Mappers,
) {

  /**
   * TMDB's score already arrives with the movie's own details, so it is read straight off the
   * loaded movie. Fetching it again from Floppy would cost a round-trip for the same number and
   * would leave the rating blank whenever no Floppy server is configured.
   */
  fun loadRatings(movie: Movie): Ratings = mappers.ratings.fromMovie(movie)
}
