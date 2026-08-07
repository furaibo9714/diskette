package io.github.furaibo9714.diskette.ui_my_shows.myshows.filters

import io.github.furaibo9714.diskette.ui_model.MyShowsSection

internal data class MyShowsFiltersUiState(
  val sectionType: MyShowsSection? = null,
  val isLoading: Boolean? = null,
)
