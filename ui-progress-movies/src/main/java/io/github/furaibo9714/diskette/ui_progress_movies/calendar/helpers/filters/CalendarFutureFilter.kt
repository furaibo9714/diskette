package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters

import io.github.furaibo9714.diskette.ui_model.Movie
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureFilter @Inject constructor() : CalendarFilter {

  override fun filter(
    now: ZonedDateTime,
    movie: Movie,
  ) = movie.released?.isAfter(now.toLocalDate()) == true || movie.released?.isEqual(now.toLocalDate()) == true
}
