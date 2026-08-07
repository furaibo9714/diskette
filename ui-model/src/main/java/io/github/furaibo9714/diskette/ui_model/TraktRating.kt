package io.github.furaibo9714.diskette.ui_model

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import java.time.ZonedDateTime

data class TraktRating(
  val idTrakt: IdTrakt,
  val rating: Int,
  val ratedAt: ZonedDateTime,
) {
  companion object {
    val EMPTY = TraktRating(IdTrakt(-1), 0, nowUtc())
  }
}
