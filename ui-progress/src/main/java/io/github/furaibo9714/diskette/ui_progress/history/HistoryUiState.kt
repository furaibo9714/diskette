package io.github.furaibo9714.diskette.ui_progress.history

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_progress.history.entities.HistoryListItem

internal data class HistoryUiState(
  val items: List<HistoryListItem> = emptyList(),
  val isLoading: Boolean = false,
  val resetScrollEvent: Event<Boolean>? = null,
)
