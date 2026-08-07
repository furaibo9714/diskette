package io.github.furaibo9714.diskette.ui_progress.progress

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_progress.progress.recycler.ProgressListItem

data class ProgressUiState(
  val items: List<ProgressListItem>? = null,
  val isLoading: Boolean = false,
  val isOverScrollEnabled: Boolean = false,
  val scrollReset: Event<Boolean>? = null,
  val sortOrder: Event<Triple<SortOrder, SortType, Boolean>>? = null,
)
