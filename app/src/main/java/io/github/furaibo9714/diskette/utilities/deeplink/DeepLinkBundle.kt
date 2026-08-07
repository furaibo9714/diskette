package io.github.furaibo9714.diskette.utilities.deeplink

import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Show

data class DeepLinkBundle(
  val show: Show? = null,
  val movie: Movie? = null,
) {

  companion object {
    val EMPTY = DeepLinkBundle()
  }
}
