package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.sorter

import io.github.furaibo9714.diskette.ui_model.Movie
import java.util.Comparator

interface CalendarSorter {
  fun sort(): Comparator<Movie>
}
