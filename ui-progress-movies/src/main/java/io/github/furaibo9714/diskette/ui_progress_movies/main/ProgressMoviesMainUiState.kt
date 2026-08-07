package io.github.furaibo9714.diskette.ui_progress_movies.main

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState
import io.github.furaibo9714.diskette.ui_model.CalendarMode

data class ProgressMoviesMainUiState(
  val timestamp: Long? = null,
  val searchQuery: String? = null,
  val calendarMode: CalendarMode? = null,
  val isSyncing: Boolean = false,
  val syncPhaseState: FloppySyncProgressState? = null,
)
