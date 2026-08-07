package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters

import io.github.furaibo9714.diskette.ui_model.Movie
import java.time.ZonedDateTime

interface CalendarFilter {
  fun filter(
    now: ZonedDateTime,
    movie: Movie,
  ): Boolean
}
