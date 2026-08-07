package io.github.furaibo9714.diskette.ui_show.sections.ratings

import io.github.furaibo9714.diskette.ui_model.Ratings
import io.github.furaibo9714.diskette.ui_model.Show

data class ShowDetailsRatingsUiState(
  val show: Show? = null,
  val ratings: Ratings? = null,
  val isRefreshingRatings: Boolean = false,
)
