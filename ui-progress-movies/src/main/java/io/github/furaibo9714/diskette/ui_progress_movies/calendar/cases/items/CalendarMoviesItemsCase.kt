package io.github.furaibo9714.diskette.ui_progress_movies.calendar.cases.items

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.toLocalZone
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.removeDiacritics
import io.github.furaibo9714.diskette.ui_model.CalendarMode.PRESENT_FUTURE
import io.github.furaibo9714.diskette.ui_model.CalendarMode.RECENTS
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters.CalendarFilter
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.groupers.CalendarGrouper
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.sorter.CalendarSorter
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

abstract class CalendarMoviesItemsCase constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
  private val imagesProvider: MovieImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  abstract val filter: CalendarFilter
  abstract val grouper: CalendarGrouper
  abstract val sorter: CalendarSorter

  suspend fun loadItems(
    searchQuery: String? = "",
    withFilters: Boolean = true,
  ): List<CalendarMovieListItem> =
    withContext(dispatchers.IO) {
      val now = nowUtc().toLocalZone()
      val language = translationsRepository.getLanguage()
      val dateFormat = dateFormatProvider.loadFullDayFormat()
      val spoilers = settingsSpoilersRepository.getAll()

      val (myMovies, watchlistMovies) = awaitAll(
        async { moviesRepository.myMovies.loadAll() },
        async { moviesRepository.watchlistMovies.loadAll() },
      )

      val elements = (myMovies + watchlistMovies)
        .filter { filter.filter(now, it) }
        .sortedWith(sorter.sort())
        .map { movie ->
          async {
            var translation: Translation? = null
            if (language != Config.DEFAULT_LANGUAGE) {
              translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
            }
            CalendarMovieListItem.MovieItem(
              movie = movie,
              image = imagesProvider.findCachedImage(movie, ImageType.POSTER),
              isWatched = myMovies.any { it.mediaId == movie.mediaId },
              isWatchlist = watchlistMovies.any { it.mediaId == movie.mediaId },
              dateFormat = dateFormat,
              translation = translation,
              spoilers = spoilers,
            )
          }
        }.awaitAll()

      val queryElements = filterByQuery(searchQuery ?: "", elements)
      val groupedItems = grouper.groupByTime(nowUtc(), queryElements)

      if (withFilters) {
        val filtersItem = when (this@CalendarMoviesItemsCase) {
          is CalendarMoviesFutureCase -> CalendarMovieListItem.Filters(PRESENT_FUTURE)
          is CalendarMoviesRecentsCase -> CalendarMovieListItem.Filters(RECENTS)
          else -> throw IllegalStateException()
        }
        listOf(filtersItem) + groupedItems
      } else {
        groupedItems
      }
    }

  private fun filterByQuery(
    query: String,
    items: List<CalendarMovieListItem.MovieItem>,
  ) = items.filter {
    it.movie.title
      .removeDiacritics()
      .contains(query, true) ||
      it.translation
        ?.title
        ?.removeDiacritics()
        ?.contains(query, true) == true ||
      it.movie.released
        ?.format(it.dateFormat)
        ?.contains(query, true) == true
  }
}
