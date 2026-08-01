package com.michaldrabik.ui_model

data class Ratings(
  val tmdb: Value? = null,
  val isHidden: Boolean = false,
  val isTapToReveal: Boolean = false,
) {

  fun isAnyLoading() = tmdb?.isLoading == true

  data class Value(
    val value: String?,
    val isLoading: Boolean,
  )
}
