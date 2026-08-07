package io.github.furaibo9714.diskette.ui_search.utilities

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType

data class SearchOptions(
  val filters: List<Mode> = emptyList(),
  val sortOrder: SortOrder = SortOrder.RANK,
  val sortType: SortType = SortType.ASCENDING,
)
