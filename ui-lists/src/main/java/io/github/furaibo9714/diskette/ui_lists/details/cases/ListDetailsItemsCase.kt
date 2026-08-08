package io.github.furaibo9714.diskette.ui_lists.details.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.Mode.MOVIES
import io.github.furaibo9714.diskette.common.Mode.SHOWS
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_lists.details.helpers.ListDetailsSorter
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_model.UserRating
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Collections
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@ViewModelScoped
class ListDetailsItemsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val listsRepository: ListsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val floppySyncManager: FloppySyncManager,
  private val sorter: ListDetailsSorter,
) {

  suspend fun loadItems(list: CustomList): Pair<List<ListDetailsItem>, Int> =
    withContext(dispatchers.IO) {
      val moviesEnabled = settingsRepository.isMoviesEnabled
      val language = translationsRepository.getLanguage()
      val listItems = listsRepository.loadItemsById(list.id)
      val spoilers = settingsRepository.spoilers.getAll()

      val showsAsync = async {
        val ids = listItems.filter { it.type == SHOWS.type }.map { it.mediaId }
        localSource.shows.getAllChunked(ids)
      }
      val moviesAsync = async {
        val ids = listItems.filter { it.type == MOVIES.type }.map { it.mediaId }
        localSource.movies.getAllChunked(ids)
      }

      val showsTranslationsAsync = async {
        if (language == Config.DEFAULT_LANGUAGE) {
          emptyMap()
        } else {
          translationsRepository.loadAllShowsLocal(language)
        }
      }
      val moviesTranslationsAsync = async {
        if (language == Config.DEFAULT_LANGUAGE) {
          emptyMap()
        } else {
          translationsRepository.loadAllMoviesLocal(language)
        }
      }

      val showsRatingsAsync = async {
        ratingsRepository.shows.loadShowsRatings()
      }
      val moviesRatingsAsync = async {
        ratingsRepository.movies.loadMoviesRatings()
      }

      val (shows, movies) = Pair(showsAsync.await(), moviesAsync.await())
      val (showsTranslations, moviesTranslations) = Pair(
        showsTranslationsAsync.await(),
        moviesTranslationsAsync.await(),
      )
      val (showsRatings, moviesRatings) = Pair(showsRatingsAsync.await(), moviesRatingsAsync.await())

      val isRankSort = list.sortByLocal == SortOrder.RANK
      val itemsToDelete = Collections.synchronizedList(mutableListOf<CustomListItem>())
      val items = listItems
        .map { listItem ->
          async {
            val listedAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(listItem.listedAt), ZoneId.of("UTC"))
            when (listItem.type) {
              SHOWS.type -> {
                val listShow = shows.firstOrNull { it.mediaId == listItem.mediaId }
                if (listShow == null) {
                  itemsToDelete.add(listItem)
                  return@async null
                }
                val show = mappers.show.fromDatabase(listShow)
                val translation = showsTranslations[show.mediaId]
                val rating = showsRatings.find { it.mediaId == show.ids.media }
                createListDetailsItem(
                  show = show,
                  listItem = listItem,
                  translation = translation,
                  userRating = rating,
                  isRankSort = isRankSort,
                  listedAt = listedAt,
                  sortOrder = list.sortByLocal,
                  spoilers = spoilers,
                )
              }
              MOVIES.type -> {
                val listMovie = movies.firstOrNull { it.mediaId == listItem.mediaId }
                if (listMovie == null) {
                  itemsToDelete.add(listItem)
                  return@async null
                }
                val movie = mappers.movie.fromDatabase(listMovie)
                val translation = moviesTranslations[movie.mediaId]
                val rating = moviesRatings.find { it.mediaId == movie.ids.media }
                createListDetailsItem(
                  movie = movie,
                  listItem = listItem,
                  translation = translation,
                  userRating = rating,
                  isRankSort = isRankSort,
                  listedAt = listedAt,
                  moviesEnabled = moviesEnabled,
                  sortOrder = list.sortByLocal,
                  spoilers = spoilers,
                )
              }
              else -> {
                throw IllegalStateException("Unsupported list item type.")
              }
            }
          }
        }.awaitAll()

      itemsToDelete.forEach {
        listsRepository.removeFromList(list.id, MediaId.parse(it.mediaId), it.type)
      }

      val sortedItems = sortItems(
        items = items.filterNotNull(),
        sort = list.sortByLocal,
        sortHow = list.sortHowLocal,
        typeFilters = list.filterTypeLocal,
      )
      Pair(sortedItems, listItems.count())
    }

  private suspend fun createListDetailsItem(
    movie: Movie,
    listItem: CustomListItem,
    translation: Translation?,
    userRating: UserRating?,
    isRankSort: Boolean,
    listedAt: ZonedDateTime,
    moviesEnabled: Boolean,
    sortOrder: SortOrder,
    spoilers: SpoilersSettings,
  ): ListDetailsItem {
    val image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)
    return ListDetailsItem(
      id = listItem.id,
      rank = listItem.rank,
      rankDisplay = listItem.rank.toInt(),
      show = null,
      movie = movie,
      image = image,
      translation = translation,
      userRating = userRating?.rating,
      isLoading = false,
      isRankDisplayed = isRankSort,
      isManageMode = false,
      isEnabled = moviesEnabled,
      isWatched = moviesRepository.myMovies.exists(movie.ids.media),
      isWatchlist = moviesRepository.watchlistMovies.exists(movie.ids.media),
      listedAt = listedAt,
      sortOrder = sortOrder,
      spoilers = spoilers,
    )
  }

  private suspend fun createListDetailsItem(
    show: Show,
    listItem: CustomListItem,
    translation: Translation?,
    userRating: UserRating?,
    isRankSort: Boolean,
    listedAt: ZonedDateTime,
    sortOrder: SortOrder,
    spoilers: SpoilersSettings,
  ): ListDetailsItem {
    val image = showImagesProvider.findCachedImage(show, ImageType.POSTER)
    return ListDetailsItem(
      id = listItem.id,
      rank = listItem.rank,
      rankDisplay = listItem.rank.toInt(),
      show = show,
      movie = null,
      image = image,
      translation = translation,
      userRating = userRating?.rating,
      isLoading = false,
      isRankDisplayed = isRankSort,
      isManageMode = false,
      isEnabled = true,
      isWatched = showsRepository.myShows.exists(show.ids.media),
      isWatchlist = showsRepository.watchlistShows.exists(show.ids.media),
      listedAt = listedAt,
      sortOrder = sortOrder,
      spoilers = spoilers,
    )
  }

  fun sortItems(
    items: List<ListDetailsItem>,
    sort: SortOrder,
    sortHow: SortType,
    typeFilters: List<Mode>,
  ) = items
    .filter {
      if (typeFilters.isEmpty()) {
        return@filter true
      }
      when {
        it.isShow() -> typeFilters.contains(SHOWS)
        it.isMovie() -> typeFilters.contains(MOVIES)
        else -> throw IllegalStateException()
      }
    }.sortedWith(sorter.sort(sort, sortHow))
    .mapIndexed { index, item ->
      val rankDisplay = if (sortHow == SortType.ASCENDING) index + 1 else items.size - index
      item.copy(
        isRankDisplayed = sort == SortOrder.RANK,
        rankDisplay = rankDisplay,
        sortOrder = sort,
      )
    }

  suspend fun deleteListItem(
    listId: Long,
    itemTraktId: MediaId,
    itemType: Mode,
  ) = withContext(dispatchers.IO) {
    val list = listsRepository.loadById(listId)
    listsRepository.removeFromList(listId, itemTraktId, itemType.type)
    floppySyncManager.scheduleListItemRemove(itemTraktId, itemType, list.idFloppy)
  }
}
