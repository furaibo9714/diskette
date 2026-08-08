package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings

import io.github.furaibo9714.diskette.ui_model.UserRating

data class RatingsUiState(
  val isLoading: Boolean? = null,
  val rating: UserRating? = null,
)
