package io.github.furaibo9714.diskette.ui_movie.sections.collections.list

import io.github.furaibo9714.diskette.repository.movies.MovieCollectionsRepository.Source
import io.github.furaibo9714.diskette.ui_model.MovieCollection

data class MovieDetailsCollectionsUiState(
  val isLoading: Boolean = true,
  val collections: Pair<List<MovieCollection>, Source>? = null,
)
