package io.github.furaibo9714.diskette.ui_model

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import java.time.ZonedDateTime

data class UserRating(
  val mediaId: MediaId,
  val rating: Int,
  val ratedAt: ZonedDateTime,
) {
  companion object {
    val EMPTY = UserRating(MediaId.EMPTY, 0, nowUtc())
  }
}
