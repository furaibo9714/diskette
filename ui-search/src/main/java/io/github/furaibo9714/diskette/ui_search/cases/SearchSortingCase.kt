package io.github.furaibo9714.diskette.ui_search.cases

import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import io.github.furaibo9714.diskette.ui_search.utilities.SearchOptions
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.Locale
import javax.inject.Inject

@ViewModelScoped
class SearchSortingCase @Inject constructor() {

  fun sort(searchOptions: SearchOptions) =
    when (searchOptions.sortType) {
      SortType.ASCENDING -> sortAscending(searchOptions.sortOrder)
      SortType.DESCENDING -> sortDescending(searchOptions.sortOrder)
    }

  private fun sortAscending(sortOrder: SortOrder) =
    when (sortOrder) {
      SortOrder.RANK -> compareBy<SearchListItem> { it.order }
      SortOrder.NAME -> compareBy { getTitle(it) }
      SortOrder.NEWEST -> compareBy { it.year }
      else -> throw IllegalStateException("Invalid sort order")
    }

  private fun sortDescending(sortOrder: SortOrder) =
    when (sortOrder) {
      SortOrder.RANK -> compareByDescending<SearchListItem> { it.order }
      SortOrder.NAME -> compareByDescending { getTitle(it) }
      SortOrder.NEWEST -> compareByDescending { it.year }
      else -> throw IllegalStateException("Invalid sort order")
    }

  private fun getTitle(item: SearchListItem): String {
    val translatedTitle =
      if (item.translation?.hasTitle == true) {
        item.translation.title
      } else {
        item.title
      }
    return translatedTitle.uppercase(Locale.ROOT)
  }
}
