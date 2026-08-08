package io.github.furaibo9714.diskette.ui_search.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.SearchResult
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@ViewModelScoped
class SearchQueryCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  suspend fun searchByQuery(query: String): List<SearchListItem> =
    withContext(dispatchers.IO) {
      val withMovies = settingsRepository.isMoviesEnabled
      val myShowsIds = showsRepository.myShows.loadAllIds()
      val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
      val myMoviesIds = moviesRepository.myMovies.loadAllIds()
      val watchlistMoviesIds = moviesRepository.watchlistMovies.loadAllIds()
      val spoilers = settingsRepository.spoilers.getAll()

      remoteSource.media
        .fetchSearch(query, withMovies)
        .mapIndexed { index, item ->
          val order = index + 1
          async {
            val result = SearchResult(
              order = order,
              show = item.show?.let { s -> mappers.show.fromNetwork(s) } ?: Show.EMPTY,
              movie = item.movie?.let { m -> mappers.movie.fromNetwork(m) } ?: Movie.EMPTY,
            )

            val isFollowed =
              if (result.isShow) {
                result.traktId in myShowsIds
              } else {
                result.traktId in myMoviesIds
              }

            val isWatchlist =
              if (result.isShow) {
                result.traktId in watchlistShowsIds
              } else {
                result.traktId in watchlistMoviesIds
              }

            val image = loadImage(result)
            val translation = loadTranslation(result)

            SearchListItem(
              id = UUID.randomUUID(),
              show = result.show,
              movie = result.movie,
              image = image,
              order = order,
              isFollowed = isFollowed,
              isWatchlist = isWatchlist,
              translation = translation,
              spoilers = spoilers,
            )
          }
        }.awaitAll()
    }

  private suspend fun loadImage(result: SearchResult) =
    when {
      result.isShow -> showsImagesProvider.findCachedImage(result.show, ImageType.POSTER)
      else -> moviesImagesProvider.findCachedImage(result.movie, ImageType.POSTER)
    }

  private suspend fun loadTranslation(result: SearchResult): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      result.isShow -> translationsRepository.loadTranslation(result.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(result.movie, language, onlyLocal = true)
    }
  }
}
