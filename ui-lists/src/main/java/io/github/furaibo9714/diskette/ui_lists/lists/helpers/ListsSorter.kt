package io.github.furaibo9714.diskette.ui_lists.lists.helpers

import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortOrder.DATE_UPDATED
import io.github.furaibo9714.diskette.ui_model.SortOrder.NAME
import io.github.furaibo9714.diskette.ui_model.SortOrder.NEWEST
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SortType.ASCENDING
import io.github.furaibo9714.diskette.ui_model.SortType.DESCENDING
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListsSorter @Inject constructor() {

  fun sort(
    sortOrder: SortOrder,
    sortType: SortType,
  ) = when (sortType) {
    ASCENDING -> sortAscending(sortOrder)
    DESCENDING -> sortDescending(sortOrder)
  }

  private fun sortAscending(sortOrder: SortOrder): Comparator<CustomList> =
    when (sortOrder) {
      NAME -> compareBy { it.name }
      NEWEST -> compareBy { it.createdAt }
      DATE_UPDATED -> compareBy { it.updatedAt }
      else -> throw IllegalStateException("Invalid sort order")
    }

  private fun sortDescending(sortOrder: SortOrder): Comparator<CustomList> =
    when (sortOrder) {
      NAME -> compareByDescending { it.name }
      NEWEST -> compareByDescending { it.createdAt }
      DATE_UPDATED -> compareByDescending { it.updatedAt }
      else -> throw IllegalStateException("Invalid sort order")
    }
}
