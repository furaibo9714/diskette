package io.github.furaibo9714.diskette.ui_progress.calendar.cases.items

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsFiltersRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_progress.calendar.helpers.WatchlistAppender
import io.github.furaibo9714.diskette.ui_progress.calendar.helpers.filters.CalendarRecentsFilter
import io.github.furaibo9714.diskette.ui_progress.calendar.helpers.groupers.CalendarRecentsGrouper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRecentsCase @Inject constructor(
  dispatchers: CoroutineDispatchers,
  localSource: LocalDataSource,
  mappers: Mappers,
  showsRepository: ShowsRepository,
  translationsRepository: TranslationsRepository,
  spoilersRepository: SettingsSpoilersRepository,
  filtersRepository: SettingsFiltersRepository,
  imagesProvider: ShowImagesProvider,
  dateFormatProvider: DateFormatProvider,
  watchlistAppender: WatchlistAppender,
  override val filter: CalendarRecentsFilter,
  override val grouper: CalendarRecentsGrouper,
) : CalendarItemsCase(
    dispatchers,
    localSource,
    mappers,
    showsRepository,
    translationsRepository,
    spoilersRepository,
    filtersRepository,
    imagesProvider,
    dateFormatProvider,
    watchlistAppender,
  ) {

  override fun sortEpisodes() =
    compareByDescending<Episode> { it.firstAired }
      .thenByDescending { it.showMediaId }
      .thenByDescending { it.episodeNumber }

  override fun isWatched(episode: Episode) = episode.isWatched

  override fun isSpoilerHidden(episode: Episode) = !episode.isWatched
}
