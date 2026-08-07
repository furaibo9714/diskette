package io.github.furaibo9714.diskette.ui_progress_movies.calendar.cases.items

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters.CalendarFutureFilter
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.groupers.CalendarFutureGrouper
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.sorter.CalendarFutureSorter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarMoviesFutureCase @Inject constructor(
  dispatchers: CoroutineDispatchers,
  moviesRepository: MoviesRepository,
  translationsRepository: TranslationsRepository,
  settingsSpoilersRepository: SettingsSpoilersRepository,
  imagesProvider: MovieImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarFutureFilter,
  override val grouper: CalendarFutureGrouper,
  override val sorter: CalendarFutureSorter,
) : CalendarMoviesItemsCase(
    dispatchers,
    moviesRepository,
    translationsRepository,
    settingsSpoilersRepository,
    imagesProvider,
    dateFormatProvider,
  )
