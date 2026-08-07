package io.github.furaibo9714.diskette.ui_discover_movies.filters.genres

import io.github.furaibo9714.diskette.ui_model.Genre

internal data class DiscoverMoviesFiltersGenresUiState(
  val genres: List<Genre>? = null,
  val isLoading: Boolean? = null,
)
