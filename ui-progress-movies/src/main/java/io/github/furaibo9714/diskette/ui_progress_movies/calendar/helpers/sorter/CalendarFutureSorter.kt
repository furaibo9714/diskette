package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.sorter

import io.github.furaibo9714.diskette.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureSorter @Inject constructor() : CalendarSorter {
  override fun sort() = compareBy<Movie> { it.released }.thenBy { it.year }
}
