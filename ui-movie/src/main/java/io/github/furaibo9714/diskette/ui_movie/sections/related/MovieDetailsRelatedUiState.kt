package io.github.furaibo9714.diskette.ui_movie.sections.related

import io.github.furaibo9714.diskette.ui_movie.sections.related.recycler.RelatedListItem

data class MovieDetailsRelatedUiState(
  val isLoading: Boolean = true,
  val relatedMovies: List<RelatedListItem>? = null,
)
