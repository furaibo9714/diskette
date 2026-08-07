package io.github.furaibo9714.diskette.ui_statistics_movies.views.ratings.recycler

import io.github.furaibo9714.diskette.ui_base.common.MovieListItem
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.TraktRating

data class StatisticsMoviesRatingItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean,
  val rating: TraktRating,
) : MovieListItem
