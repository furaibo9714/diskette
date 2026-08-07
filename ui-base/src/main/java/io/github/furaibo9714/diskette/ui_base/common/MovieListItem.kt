package io.github.furaibo9714.diskette.ui_base.common

import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie

interface MovieListItem {
  val movie: Movie
  val image: Image
  val isLoading: Boolean

  infix fun isSameAs(other: MovieListItem) = movie.ids.trakt == other.movie.ids.trakt
}
