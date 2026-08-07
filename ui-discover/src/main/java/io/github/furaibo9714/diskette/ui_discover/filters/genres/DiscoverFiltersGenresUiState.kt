package io.github.furaibo9714.diskette.ui_discover.filters.genres

import io.github.furaibo9714.diskette.ui_model.Genre

internal data class DiscoverFiltersGenresUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
