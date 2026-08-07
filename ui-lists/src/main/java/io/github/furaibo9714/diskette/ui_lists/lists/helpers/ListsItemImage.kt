package io.github.furaibo9714.diskette.ui_lists.lists.helpers

import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Show

data class ListsItemImage(
  val image: Image,
  val show: Show? = null,
  val movie: Movie? = null,
) {

  fun getIds(): Ids? {
    if (show != null) return show.ids
    if (movie != null) return movie.ids
    return null
  }

  fun isShow() = show != null

  fun isMovie() = movie != null
}
