package io.github.furaibo9714.diskette.ui_search.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.MovieSearch
import io.github.furaibo9714.diskette.data_local.database.model.ShowSearch
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.SearchResult
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class SearchSuggestionsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  private var showsCache: List<ShowSearch>? = null
  private var moviesCache: List<MovieSearch>? = null
  /** Keyed by [MediaId.key] to match the cached rows, which hold the raw storage form. */
  private var showTranslationsCache: Map<String, Translation>? = null
  private var movieTranslationsCache: Map<String, Translation>? = null

  suspend fun loadSuggestions(query: String) =
    withContext(dispatchers.IO) {
      preloadCache()
      val spoilers = settingsRepository.spoilers.getAll()

      val showsDef = async { loadShows(query.trim(), 5) }
      val moviesDef = async { loadMovies(query.trim(), 5) }

      val suggestions = (showsDef.await() + moviesDef.await()).map {
        when (it) {
          is Show -> SearchResult(0, it, Movie.EMPTY)
          is Movie -> SearchResult(0, Show.EMPTY, it)
          else -> throw IllegalStateException()
        }
      }

      suggestions
        .map {
          async {
            val isFollowed =
              if (it.isShow) {
                showsRepository.myShows.exists(it.show.ids.media)
              } else {
                moviesRepository.myMovies.exists(it.movie.ids.media)
              }

            val isWatchlist =
              if (it.isShow) {
                showsRepository.watchlistShows.exists(it.show.ids.media)
              } else {
                moviesRepository.watchlistMovies.exists(it.movie.ids.media)
              }

            val image =
              if (it.isShow) {
                showsImagesProvider.findCachedImage(it.show, ImageType.POSTER)
              } else {
                moviesImagesProvider.findCachedImage(it.movie, ImageType.POSTER)
              }

            SearchListItem(
              id = UUID.randomUUID(),
              show = it.show,
              movie = it.movie,
              image = image,
              order = it.order,
              isFollowed = isFollowed,
              isWatchlist = isWatchlist,
              translation = loadTranslation(it),
              spoilers = spoilers,
            )
          }
        }.awaitAll()
        .sortedByDescending { it.votes }
    }

  suspend fun preloadCache() =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      val moviesEnabled = settingsRepository.isMoviesEnabled

      if (showsCache == null) {
        showsCache = localSource.shows.getAllForSearch()
      }
      if (moviesEnabled && moviesCache == null) {
        moviesCache = localSource.movies.getAllForSearch()
      }

      if (translationsRepository.getLanguage() != Config.DEFAULT_LANGUAGE) {
        if (showTranslationsCache == null) {
          showTranslationsCache = translationsRepository.loadAllShowsLocal(language).mapKeys { (id, _) -> id.key }
        }
        if (moviesEnabled && movieTranslationsCache == null) {
          movieTranslationsCache = translationsRepository.loadAllMoviesLocal(language).mapKeys { (id, _) -> id.key }
        }
      }
    }

  private suspend fun loadShows(
    query: String,
    limit: Int,
  ): List<Show> {
    if (query.trim().isBlank()) {
      return emptyList()
    }

    val cachedIds = showsCache
      ?.filter {
        it.title.contains(query, true) ||
          showTranslationsCache?.get(it.mediaId)?.title?.contains(query, true) == true
      }?.take(limit)
      ?.map { it.mediaId }

    return localSource.shows
      .getAll(cachedIds ?: emptyList())
      .map { mappers.show.fromDatabase(it) }
  }

  private suspend fun loadMovies(
    query: String,
    limit: Int,
  ): List<Movie> {
    if (query.trim().isBlank()) {
      return emptyList()
    }

    val cachedIds = moviesCache
      ?.filter {
        it.title.contains(query, true) ||
          movieTranslationsCache?.get(it.mediaId)?.title?.contains(query, true) == true
      }?.take(limit)
      ?.map { it.mediaId }

    return localSource.movies
      .getAll(cachedIds ?: emptyList())
      .map { mappers.movie.fromDatabase(it) }
  }

  private suspend fun loadTranslation(result: SearchResult): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      result.isShow -> translationsRepository.loadTranslation(result.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(result.movie, language, onlyLocal = true)
    }
  }

  fun clearCache() {
    showsCache = null
    moviesCache = null
    showTranslationsCache = null
    movieTranslationsCache = null
  }
}
