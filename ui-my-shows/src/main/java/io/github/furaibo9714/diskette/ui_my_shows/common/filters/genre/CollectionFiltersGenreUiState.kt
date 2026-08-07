package io.github.furaibo9714.diskette.ui_my_shows.common.filters.genre

import io.github.furaibo9714.diskette.ui_model.Genre

internal data class CollectionFiltersGenreUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
