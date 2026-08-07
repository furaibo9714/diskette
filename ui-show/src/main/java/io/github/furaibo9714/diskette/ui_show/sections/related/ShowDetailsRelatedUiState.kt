package io.github.furaibo9714.diskette.ui_show.sections.related

import io.github.furaibo9714.diskette.ui_show.sections.related.recycler.RelatedListItem

data class ShowDetailsRelatedUiState(
  val isLoading: Boolean = true,
  val relatedShows: List<RelatedListItem>? = null,
)
