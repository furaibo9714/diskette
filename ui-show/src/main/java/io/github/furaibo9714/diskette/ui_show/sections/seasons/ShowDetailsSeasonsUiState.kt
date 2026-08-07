package io.github.furaibo9714.diskette.ui_show.sections.seasons

import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem

data class ShowDetailsSeasonsUiState(
  val isLoading: Boolean = true,
  val seasons: List<SeasonListItem>? = null,
)
