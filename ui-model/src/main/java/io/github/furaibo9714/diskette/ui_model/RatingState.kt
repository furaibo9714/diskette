package io.github.furaibo9714.diskette.ui_model

data class RatingState(
  val userRating: TraktRating? = null,
  val rateLoading: Boolean? = null,
) {

  fun hasRating() = userRating != null && userRating.rating > 0
}
