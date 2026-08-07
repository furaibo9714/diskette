package io.github.furaibo9714.diskette.ui_progress.calendar

import io.github.furaibo9714.diskette.ui_model.CalendarMode
import io.github.furaibo9714.diskette.ui_progress.calendar.recycler.CalendarListItem

data class CalendarUiState(
  val items: List<CalendarListItem>? = null,
  val mode: CalendarMode = CalendarMode.PRESENT_FUTURE,
)
