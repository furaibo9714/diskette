package io.github.furaibo9714.diskette.ui_discover_movies.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtcDay
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_discover_movies.helpers.itemtype.ImageTypeProvider
import io.github.furaibo9714.diskette.ui_discover_movies.recycler.DiscoverMovieListItem
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed
import io.github.furaibo9714.diskette.ui_model.DiscoverFilters
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
internal class DiscoverMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val imagesProvider: MovieImagesProvider,
  private val imageTypeProvider: ImageTypeProvider,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun isCacheValid() =
    withContext(dispatchers.IO) {
      moviesRepository.discoverMovies.isCacheValid()
    }

  suspend fun loadCachedMovies(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val myIds = async { moviesRepository.myMovies.loadAllIds() }
      val watchlistIds = async { moviesRepository.watchlistMovies.loadAllIds() }
      val hiddenIds = async { moviesRepository.hiddenMovies.loadAllIds() }
      val cachedMovies = async { moviesRepository.discoverMovies.loadAllCached() }
      val language = translationsRepository.getLanguage()

      prepareItems(
        cachedMovies.await(),
        myIds.await(),
        watchlistIds.await(),
        hiddenIds.await(),
        filters,
        language,
      )
    }

  suspend fun loadRemoteMovies(filters: DiscoverFilters) =
    withContext(dispatchers.IO) {
      val showCollection = !filters.hideCollection
      val genres = filters.genres.toList()

      val myAsync = async { moviesRepository.myMovies.loadAllIds() }
      val watchlistSync = async { moviesRepository.watchlistMovies.loadAllIds() }
      val hiddenAsync = async { moviesRepository.hiddenMovies.loadAllIds() }
      val (myIds, watchlistIds, hiddenIds) = awaitAll(myAsync, watchlistSync, hiddenAsync)
      val collectionSize = myIds.size + watchlistIds.size + hiddenIds.size

      val remoteMovies = moviesRepository.discoverMovies.loadAllRemote(
        filters.feedOrder,
        showCollection,
        collectionSize,
        genres,
      )
      val language = translationsRepository.getLanguage()

      moviesRepository.discoverMovies.cacheDiscoverMovies(remoteMovies)
      prepareItems(remoteMovies, myIds, watchlistIds, hiddenIds, filters, language)
    }

  private suspend fun prepareItems(
    movies: List<Movie>,
    myMoviesIds: List<Long>,
    watchlistMoviesIds: List<Long>,
    hiddenMoviesIds: List<Long>,
    filters: DiscoverFilters,
    language: String,
  ) = coroutineScope {
    val collectionIds = myMoviesIds + watchlistMoviesIds
    movies
      .filter { !hiddenMoviesIds.contains(it.mediaId) }
      .filter {
        if (!filters.hideCollection) {
          true
        } else {
          !collectionIds.contains(it.mediaId)
        }
      }.sortedBy(filters.feedOrder)
      .mapIndexed { index, movie ->
        async {
          val itemType = imageTypeProvider.getImageType(index)
          val image = imagesProvider.findCachedImage(movie, itemType)
          val translation = loadTranslation(language, itemType, movie)
          DiscoverMovieListItem(
            movie,
            image,
            isCollected = movie.ids.media.key in myMoviesIds,
            isWatchlist = movie.ids.media.key in watchlistMoviesIds,
            translation = translation,
          )
        }
      }.awaitAll()
      .toList()
  }

  private suspend fun loadTranslation(
    language: String,
    itemType: ImageType,
    movie: Movie,
  ) = if (language == Config.DEFAULT_LANGUAGE || itemType == POSTER) {
    null
  } else {
    translationsRepository.loadTranslation(movie, language, true)
  }

  private fun List<Movie>.sortedBy(order: DiscoverFeed): List<Movie> {
    val nowUtcDay = nowUtcDay()
    return when (order) {
      DiscoverFeed.RECENT -> {
        this
          .filter {
            it.released != null && (it.released!!.isBefore(nowUtcDay) || it.released!!.isEqual(nowUtcDay))
          }.sortedWith(compareByDescending { it.released })
      }
      else -> {
        this
      }
    }
  }
}
