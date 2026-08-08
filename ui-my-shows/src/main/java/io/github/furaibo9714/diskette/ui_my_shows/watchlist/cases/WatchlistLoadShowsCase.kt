package io.github.furaibo9714.diskette.ui_my_shows.watchlist.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_model.UserRating
import io.github.furaibo9714.diskette.ui_my_shows.common.recycler.CollectionListItem
import io.github.furaibo9714.diskette.ui_my_shows.watchlist.helpers.WatchlistItemFilter
import io.github.furaibo9714.diskette.ui_my_shows.watchlist.helpers.WatchlistItemSorter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@ViewModelScoped
class WatchlistLoadShowsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsCase: WatchlistRatingsCase,
  private val sorter: WatchlistItemSorter,
  private val filters: WatchlistItemFilter,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadShows(searchQuery: String): List<CollectionListItem> =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      val ratings = ratingsCase.loadRatings()
      val dateFormat = dateFormatProvider.loadFullDayFormat()
      val translations =
        if (language == Config.DEFAULT_LANGUAGE) {
          emptyMap()
        } else {
          translationsRepository.loadAllShowsLocal(language)
        }
      val spoilers = settingsRepository.spoilers.getAll()

      var filtersItem = loadFiltersItem()
      val filtersNetworks = filtersItem.networks
        .flatMap { network -> network.channels.map { it } }
      val filtersGenres = filtersItem.genres.map { it.slug.lowercase() }

      val showsItems = showsRepository.watchlistShows
        .loadAll()
        .map {
          toListItemAsync(
            show = it,
            translation = translations[it.traktId],
            userRating = ratings[it.ids.trakt],
            dateFormat = dateFormat,
            sortOrder = filtersItem.sortOrder,
            spoilers = spoilers,
          )
        }.awaitAll()
        .filter { item ->
          filters.filterByQuery(item, searchQuery) &&
            filters.filterUpcoming(item, filtersItem.upcoming) &&
            filters.filterNetworks(item, filtersNetworks) &&
            filters.filterGenres(item, filtersGenres)
        }.sortedWith(sorter.sort(filtersItem.sortOrder, filtersItem.sortType))

      filtersItem = filtersItem.copy(count = showsItems.size)

      if (showsItems.isNotEmpty() || filtersItem.hasActiveFilters()) {
        listOf(filtersItem) + showsItems
      } else {
        showsItems
      }
    }

  private fun loadFiltersItem(): CollectionListItem.FiltersItem =
    CollectionListItem.FiltersItem(
      sortOrder = settingsRepository.sorting.watchlistShowsSortOrder,
      sortType = settingsRepository.sorting.watchlistShowsSortType,
      networks = settingsRepository.filters.watchlistShowsNetworks,
      genres = settingsRepository.filters.watchlistShowsGenres,
      upcoming = settingsRepository.filters.watchlistShowsUpcoming,
      count = 0,
    )

  private fun CoroutineScope.toListItemAsync(
    show: Show,
    translation: Translation?,
    userRating: UserRating?,
    dateFormat: DateTimeFormatter,
    sortOrder: SortOrder,
    spoilers: SpoilersSettings,
  ) = async {
    val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
    CollectionListItem.ShowItem(
      isLoading = false,
      show = show,
      image = image,
      dateFormat = dateFormat,
      translation = translation,
      userRating = userRating?.rating,
      sortOrder = sortOrder,
      spoilers = CollectionListItem.ShowItem.Spoilers(
        isSpoilerHidden = spoilers.isWatchlistShowsHidden,
        isSpoilerRatingsHidden = spoilers.isWatchlistShowsRatingsHidden,
        isSpoilerTapToReveal = spoilers.isTapToReveal,
      ),
    )
  }
}
