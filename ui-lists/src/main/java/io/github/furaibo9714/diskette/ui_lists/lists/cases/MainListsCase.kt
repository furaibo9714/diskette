package io.github.furaibo9714.diskette.ui_lists.lists.cases

import io.github.furaibo9714.diskette.common.Mode.MOVIES
import io.github.furaibo9714.diskette.common.Mode.SHOWS
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_lists.lists.helpers.ListsItemImage
import io.github.furaibo9714.diskette.ui_lists.lists.helpers.ListsSorter
import io.github.furaibo9714.diskette.ui_lists.lists.recycler.ListsItem
import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MainListsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  private val dateProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val sorter: ListsSorter,
) {

  companion object {
    private const val IMAGES_LIMIT = 3
  }

  suspend fun loadLists(searchQuery: String?) =
    withContext(dispatchers.IO) {
      val lists = listsRepository.loadAll()
      val dateFormat = dateProvider.loadFullDayFormat()
      val sorting = Pair(
        settingsRepository.sorting.listsAllSortOrder,
        settingsRepository.sorting.listsAllSortType,
      )

      lists
        .filterByQuery(searchQuery)
        .sortedWith(sorter.sort(sorting.first, sorting.second))
        .map {
          async {
            val items = localSource.customListsItems.getItemsForListImages(it.id, IMAGES_LIMIT)
            val images = mutableListOf<ListsItemImage>()
            val unavailable = ListsItemImage(Image.createUnavailable(POSTER))
            items.forEach { item ->
              images.add(findImage(item) ?: unavailable)
            }
            if (images.size < IMAGES_LIMIT) {
              (images.size..IMAGES_LIMIT).forEach { _ -> images.add(unavailable) }
            }
            ListsItem(it, images, sorting, dateFormat)
          }
        }.awaitAll()
    }

  private fun List<CustomList>.filterByQuery(query: String?) =
    when {
      query.isNullOrBlank() -> this
      else -> this.filter {
        it.name.contains(query, ignoreCase = true) ||
          it.description?.contains(query, ignoreCase = true) == true
      }
    }

  private suspend fun findImage(item: CustomListItem) =
    when (item.type) {
      SHOWS.type -> {
        val showDb = localSource.shows.getById(item.mediaId)
        showDb?.let {
          val show = mappers.show.fromDatabase(it)
          val image = showImagesProvider.findCachedImage(show, POSTER)
          ListsItemImage(image, show = show)
        }
      }
      MOVIES.type -> {
        val movieDb = localSource.movies.getById(item.mediaId)
        movieDb?.let {
          val movie = mappers.movie.fromDatabase(movieDb)
          val image = movieImagesProvider.findCachedImage(movie, POSTER)
          ListsItemImage(image, movie = movie)
        }
      }
      else -> {
        throw IllegalStateException()
      }
    }
}
