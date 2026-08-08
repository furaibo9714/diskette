package io.github.furaibo9714.diskette.ui_model

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import java.time.ZonedDateTime

data class UserRating(
  val idTrakt: IdTrakt,
  val rating: Int,
  val ratedAt: ZonedDateTime,
) {
  companion object {
    val EMPTY = UserRating(IdTrakt(-1), 0, nowUtc())
  }
}
