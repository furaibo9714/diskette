package io.github.furaibo9714.diskette.ui_discover.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.isSameDayOrAfter
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.toUtcDateTime
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_discover.helpers.itemtype.ImageTypeProvider
import io.github.furaibo9714.diskette.ui_discover.recycler.DiscoverListItem
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed
import io.github.furaibo9714.diskette.ui_model.DiscoverFilters
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
internal class DiscoverShowsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val imageTypeProvider: ImageTypeProvider,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun isCacheValid() =
    withContext(dispatchers.IO) {
      showsRepository.discoverShows.isCacheValid()
    }

  suspend fun loadCachedShows(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val myShowsIds = async { showsRepository.myShows.loadAllIds() }
      val watchlistShowsIds = async { showsRepository.watchlistShows.loadAllIds() }
      val archiveShowsIds = async { showsRepository.hiddenShows.loadAllIds() }
      val cachedShows = async { showsRepository.discoverShows.loadAllCached() }

      prepareItems(
        shows = cachedShows.await(),
        myShowsIds = myShowsIds.await(),
        watchlistShowsIds = watchlistShowsIds.await(),
        hiddenShowsIds = archiveShowsIds.await(),
        filters = filters,
      )
    }

  suspend fun loadRemoteShows(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val showCollection = !filters.hideCollection
      val genres = filters.genres.toList()
      val networks = filters.networks.toList()

      val myAsync = async { showsRepository.myShows.loadAllIds() }
      val watchlistSync = async { showsRepository.watchlistShows.loadAllIds() }
      val archiveAsync = async { showsRepository.hiddenShows.loadAllIds() }
      val (myIds, watchlistIds, hiddenIds) = awaitAll(myAsync, watchlistSync, archiveAsync)
      val collectionSize = myIds.size + watchlistIds.size + hiddenIds.size

      val remoteShows = showsRepository.discoverShows.loadAllRemote(
        order = filters.feedOrder,
        showCollection = showCollection,
        collectionSize = collectionSize,
        genres = genres,
        networks = networks,
      )

      showsRepository.discoverShows.cacheDiscoverShows(remoteShows)

      prepareItems(
        shows = remoteShows,
        myShowsIds = myIds,
        watchlistShowsIds = watchlistIds,
        hiddenShowsIds = hiddenIds,
        filters = filters,
      )
    }

  private suspend fun prepareItems(
    shows: List<Show>,
    myShowsIds: List<MediaId>,
    watchlistShowsIds: List<MediaId>,
    hiddenShowsIds: List<MediaId>,
    filters: DiscoverFilters,
  ) = coroutineScope {
    val language = translationsRepository.getLanguage()
    val collectionIds = myShowsIds + watchlistShowsIds + hiddenShowsIds
    shows
      .filter { it.mediaId !in hiddenShowsIds }
      .filter {
        if (!filters.hideCollection) {
          true
        } else {
          it.mediaId !in collectionIds
        }
      }.sortedBy(filters.feedOrder)
      .mapIndexed { index, show ->
        async {
          val itemType = imageTypeProvider.getImageType(index)
          val image = imagesProvider.findCachedImage(show, itemType)
          val translation = loadTranslation(language, itemType, show)
          DiscoverListItem(
            show = show,
            image = image,
            isFollowed = show.mediaId in myShowsIds,
            isWatchlist = show.mediaId in watchlistShowsIds,
            translation = translation,
          )
        }
      }.awaitAll()
  }

  private suspend fun loadTranslation(
    language: String,
    itemType: ImageType,
    show: Show,
  ) = if (language == Config.DEFAULT_LANGUAGE || itemType == ImageType.POSTER) {
    null
  } else {
    translationsRepository.loadTranslation(show, language, true)
  }

  private fun List<Show>.sortedBy(order: DiscoverFeed): List<Show> {
    val nowUtc = nowUtc()
    return when (order) {
      DiscoverFeed.RECENT -> {
        this
          .filter {
            it.firstAired.isNotBlank() &&
              nowUtc.isSameDayOrAfter(it.firstAired.toUtcDateTime() ?: return@filter false)
          }.sortedWith(compareByDescending { it.firstAired })
      }
      else -> {
        this
      }
    }
  }
}
