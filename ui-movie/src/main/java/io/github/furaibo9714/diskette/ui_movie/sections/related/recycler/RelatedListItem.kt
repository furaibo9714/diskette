package io.github.furaibo9714.diskette.ui_movie.sections.related.recycler

import io.github.furaibo9714.diskette.ui_base.common.MovieListItem
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie

data class RelatedListItem(
  override val movie: Movie,
  override val image: Image,
  override var isLoading: Boolean = false,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false,
) : MovieListItem
