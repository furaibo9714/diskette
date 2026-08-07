package io.github.furaibo9714.diskette.ui_statistics_movies

import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem

data class StatisticsMoviesUiState(
  val totalTimeSpentMinutes: Int? = null,
  val totalWatchedMovies: Int? = null,
  val topGenres: List<Genre>? = null,
  val ratings: List<StatisticsMoviesRatingItem>? = null,
)
