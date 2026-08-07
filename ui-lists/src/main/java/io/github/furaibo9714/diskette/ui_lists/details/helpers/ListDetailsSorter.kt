package io.github.furaibo9714.diskette.ui_lists.details.helpers

import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortOrder.DATE_ADDED
import io.github.furaibo9714.diskette.ui_model.SortOrder.NAME
import io.github.furaibo9714.diskette.ui_model.SortOrder.NEWEST
import io.github.furaibo9714.diskette.ui_model.SortOrder.RANDOM
import io.github.furaibo9714.diskette.ui_model.SortOrder.RANK
import io.github.furaibo9714.diskette.ui_model.SortOrder.RATING
import io.github.furaibo9714.diskette.ui_model.SortOrder.USER_RATING
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SortType.ASCENDING
import io.github.furaibo9714.diskette.ui_model.SortType.DESCENDING
import java.util.UUID
import javax.inject.Inject

class ListDetailsSorter @Inject constructor() {

  fun sort(
    sortOrder: SortOrder,
    sortType: SortType,
  ) = when (sortType) {
    ASCENDING -> sortAscending(sortOrder)
    DESCENDING -> sortDescending(sortOrder)
  }

  private fun sortAscending(sortOrder: SortOrder): Comparator<ListDetailsItem> =
    when (sortOrder) {
      RANK -> {
        compareBy { it.rank }
      }
      NAME -> {
        compareBy { getTitle(it) }
      }
      NEWEST -> {
        compareBy<ListDetailsItem> { it.getYear() }.thenBy { it.getDate() }
      }
      RATING -> {
        compareBy { it.getRating() }
      }
      USER_RATING -> {
        compareByDescending<ListDetailsItem> { it.userRating != null }
          .thenBy { it.userRating }
          .thenBy { getTitle(it) }
      }
      DATE_ADDED -> {
        compareBy { it.listedAt }
      }
      RANDOM -> {
        compareBy { UUID.randomUUID() }
      }
      else -> {
        throw IllegalStateException("Invalid sort order")
      }
    }

  private fun sortDescending(sortOrder: SortOrder): Comparator<ListDetailsItem> =
    when (sortOrder) {
      RANK -> {
        compareByDescending { it.rank }
      }
      NAME -> {
        compareByDescending { getTitle(it) }
      }
      NEWEST -> {
        compareByDescending<ListDetailsItem> { it.getYear() }.thenByDescending { it.getDate() }
      }
      RATING -> {
        compareByDescending { it.getRating() }
      }
      USER_RATING -> {
        compareByDescending<ListDetailsItem> { it.userRating != null }
          .thenByDescending { it.userRating }
          .thenBy { getTitle(it) }
      }
      DATE_ADDED -> {
        compareByDescending { it.listedAt }
      }
      RANDOM -> {
        compareBy { UUID.randomUUID() }
      }
      else -> {
        throw IllegalStateException("Invalid sort order")
      }
    }

  private fun getTitle(item: ListDetailsItem): String =
    if (item.translation?.hasTitle == true) {
      item.translation.title
    } else {
      item.getTitleNoThe().uppercase()
    }
}
