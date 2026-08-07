package io.github.furaibo9714.diskette.ui_movie.sections.ratings

import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Ratings

data class MovieDetailsRatingsUiState(
  val movie: Movie? = null,
  val ratings: Ratings? = null,
  val isRefreshingRatings: Boolean = false,
)
