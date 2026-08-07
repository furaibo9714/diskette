package io.github.furaibo9714.diskette.ui_lists.lists.recycler

import io.github.furaibo9714.diskette.ui_lists.lists.helpers.ListsItemImage
import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import java.time.format.DateTimeFormatter

data class ListsItem(
  val list: CustomList,
  val images: List<ListsItemImage>,
  val sortOrder: Pair<SortOrder, SortType>,
  val dateFormat: DateTimeFormatter? = null,
)
