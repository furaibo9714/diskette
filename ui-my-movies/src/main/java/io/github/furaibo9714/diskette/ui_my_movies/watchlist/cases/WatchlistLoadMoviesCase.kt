package io.github.furaibo9714.diskette.ui_my_movies.watchlist.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_model.UserRating
import io.github.furaibo9714.diskette.ui_my_movies.common.helpers.CollectionItemFilter
import io.github.furaibo9714.diskette.ui_my_movies.common.helpers.CollectionItemSorter
import io.github.furaibo9714.diskette.ui_my_movies.common.recycler.CollectionListItem
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@ViewModelScoped
class WatchlistLoadMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsCase: WatchlistRatingsCase,
  private val sorter: CollectionItemSorter,
  private val filters: CollectionItemFilter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadMovies(searchQuery: String): List<CollectionListItem> =
    withContext(dispatchers.IO) {
      val ratings = ratingsCase.loadRatings()
      val dateFormat = dateFormatProvider.loadShortDayFormat()
      val fullDateFormat = dateFormatProvider.loadFullDayFormat()
      val language = translationsRepository.getLanguage()
      val spoilers = settingsRepository.spoilers.getAll()
      val translations = if (language == Config.DEFAULT_LANGUAGE) {
        emptyMap()
      } else {
        translationsRepository.loadAllMoviesLocal(language)
      }

      var filtersItem = loadFiltersItem()
      val filtersGenres = filtersItem.genres.map { it.slug.lowercase() }

      val moviesItems = moviesRepository.watchlistMovies
        .loadAll()
        .map {
          toListItemAsync(
            movie = it,
            translation = translations[it.mediaId],
            userRating = ratings[it.ids.media],
            dateFormat = dateFormat,
            fullDateFormat = fullDateFormat,
            sortOrder = filtersItem.sortOrder,
            spoilers = spoilers,
          )
        }.awaitAll()
        .filter {
          filters.filterByQuery(it, searchQuery) &&
            filters.filterUpcoming(it, filtersItem.upcoming) &&
            filters.filterGenres(it, filtersGenres)
        }.sortedWith(sorter.sort(filtersItem.sortOrder, filtersItem.sortType))

      filtersItem = filtersItem.copy(count = moviesItems.size)

      if (moviesItems.isNotEmpty() || filtersItem.hasActiveFilters()) {
        listOf(filtersItem) + moviesItems
      } else {
        moviesItems
      }
    }

  private fun loadFiltersItem(): CollectionListItem.FiltersItem =
    CollectionListItem.FiltersItem(
      sortOrder = settingsRepository.sorting.watchlistMoviesSortOrder,
      sortType = settingsRepository.sorting.watchlistMoviesSortType,
      genres = settingsRepository.filters.watchlistMoviesGenres,
      upcoming = settingsRepository.filters.watchlistMoviesUpcoming,
      count = 0,
    )

  suspend fun loadTranslation(
    movie: Movie,
    onlyLocal: Boolean,
  ): Translation? =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      if (language == Config.DEFAULT_LANGUAGE) {
        return@withContext Translation.EMPTY
      }
      translationsRepository.loadTranslation(movie, language, onlyLocal)
    }

  private fun CoroutineScope.toListItemAsync(
    movie: Movie,
    translation: Translation?,
    userRating: UserRating?,
    dateFormat: DateTimeFormatter,
    fullDateFormat: DateTimeFormatter,
    sortOrder: SortOrder,
    spoilers: SpoilersSettings,
  ) = async {
    CollectionListItem.MovieItem(
      isLoading = false,
      movie = movie,
      image = imagesProvider.findCachedImage(movie, ImageType.POSTER),
      dateFormat = dateFormat,
      fullDateFormat = fullDateFormat,
      translation = translation,
      userRating = userRating?.rating,
      sortOrder = sortOrder,
      spoilers = CollectionListItem.MovieItem.Spoilers(
        isSpoilerHidden = spoilers.isWatchlistMoviesHidden,
        isSpoilerRatingsHidden = spoilers.isWatchlistMoviesRatingsHidden,
        isSpoilerTapToReveal = spoilers.isTapToReveal,
      ),
    )
  }
}
