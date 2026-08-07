package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.helpers

import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import java.time.format.DateTimeFormatter

data class MovieContextItem(
  val movie: Movie,
  val image: Image,
  val translation: Translation?,
  val dateFormat: DateTimeFormatter?,
  val userRating: Int?,
  val isMyMovie: Boolean,
  val isWatchlist: Boolean,
  val isHidden: Boolean,
  val isPinnedTop: Boolean,
  val spoilers: SpoilersSettings,
) {

  fun isInCollection() = isHidden || isWatchlist || isMyMovie
}
