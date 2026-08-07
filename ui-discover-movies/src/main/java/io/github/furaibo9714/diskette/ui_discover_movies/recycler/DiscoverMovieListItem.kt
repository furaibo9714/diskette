package io.github.furaibo9714.diskette.ui_discover_movies.recycler

import io.github.furaibo9714.diskette.ui_base.common.MovieListItem
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Translation

data class DiscoverMovieListItem(
  override val movie: Movie,
  override val image: Image,
  override var isLoading: Boolean = false,
  val isCollected: Boolean = false,
  val isWatchlist: Boolean = false,
  val translation: Translation? = null,
) : MovieListItem
