package io.github.furaibo9714.diskette.ui_lists.details

import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import io.github.furaibo9714.diskette.ui_model.CustomList

data class ListDetailsUiState(
  val listDetails: CustomList? = null,
  val listItems: List<ListDetailsItem>? = null,
  val resetScroll: Event<Boolean>? = null,
  val deleteEvent: Event<Boolean>? = null,
  val isFiltersVisible: Boolean = false,
  val isManageMode: Boolean = false,
  val isLoading: Boolean = false,
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
)
