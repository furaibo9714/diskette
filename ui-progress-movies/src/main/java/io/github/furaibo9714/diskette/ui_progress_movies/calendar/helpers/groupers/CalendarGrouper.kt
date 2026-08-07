package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.groupers

import io.github.furaibo9714.diskette.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import java.time.ZonedDateTime

interface CalendarGrouper {
  fun groupByTime(
    nowUtc: ZonedDateTime,
    items: List<CalendarMovieListItem.MovieItem>,
  ): List<CalendarMovieListItem>
}
