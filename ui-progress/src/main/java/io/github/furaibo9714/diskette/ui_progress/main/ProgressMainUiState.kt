package io.github.furaibo9714.diskette.ui_progress.main

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.CalendarMode

data class ProgressMainUiState(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val resetScroll: Event<Boolean>? = null,
  val isSyncing: Boolean = false,
  val syncPhaseState: FloppySyncProgressState? = null,
)
