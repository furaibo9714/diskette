package io.github.furaibo9714.diskette.ui_progress_movies.calendar.cases.items

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters.CalendarRecentsFilter
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.groupers.CalendarRecentsGrouper
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.sorter.CalendarRecentsSorter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarMoviesRecentsCase @Inject constructor(
  dispatchers: CoroutineDispatchers,
  moviesRepository: MoviesRepository,
  translationsRepository: TranslationsRepository,
  settingsSpoilersRepository: SettingsSpoilersRepository,
  imagesProvider: MovieImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarRecentsFilter,
  override val grouper: CalendarRecentsGrouper,
  override val sorter: CalendarRecentsSorter,
) : CalendarMoviesItemsCase(
    dispatchers,
    moviesRepository,
    translationsRepository,
    settingsSpoilersRepository,
    imagesProvider,
    dateFormatProvider,
  )
