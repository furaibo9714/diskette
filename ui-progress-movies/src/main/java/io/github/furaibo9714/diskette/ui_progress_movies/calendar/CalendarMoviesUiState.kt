package io.github.furaibo9714.diskette.ui_progress_movies.calendar

import io.github.furaibo9714.diskette.ui_model.CalendarMode
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.recycler.CalendarMovieListItem

data class CalendarMoviesUiState(
  val items: List<CalendarMovieListItem>? = null,
  val mode: CalendarMode = CalendarMode.PRESENT_FUTURE,
)
