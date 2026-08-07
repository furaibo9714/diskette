package io.github.furaibo9714.diskette.ui_progress.calendar.helpers.groupers

import io.github.furaibo9714.diskette.ui_progress.calendar.recycler.CalendarListItem

interface CalendarGrouper {
  fun groupByTime(items: List<CalendarListItem.Episode>): List<CalendarListItem>
}
