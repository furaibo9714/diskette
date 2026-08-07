package io.github.furaibo9714.diskette.ui_lists.lists

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_lists.lists.recycler.ListsItem
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType

data class ListsUiState(
  val items: List<ListsItem>? = null,
  val resetScroll: Event<Boolean> = Event(false),
  val sortOrder: Pair<SortOrder, SortType>? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
)
