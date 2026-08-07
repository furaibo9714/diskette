package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings

import io.github.furaibo9714.diskette.ui_model.TraktRating

data class RatingsUiState(
  val isLoading: Boolean? = null,
  val rating: TraktRating? = null,
)
