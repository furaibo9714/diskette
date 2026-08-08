package io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases

import io.github.furaibo9714.diskette.common.Config.DEFAULT_LANGUAGE
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MovieCollectionsRepository
import io.github.furaibo9714.diskette.repository.movies.MyMoviesRepository
import io.github.furaibo9714.diskette.repository.movies.WatchlistMoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val collectionsRepository: MovieCollectionsRepository,
  private val myMoviesRepository: MyMoviesRepository,
  private val watchlistMoviesRepository: WatchlistMoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
  private val imagesProvider: MovieImagesProvider,
) {

  suspend fun loadCollectionMovies(
    collectionId: MediaId,
    language: String,
  ): List<MovieDetailsCollectionItem.MovieItem> =
    withContext(dispatchers.IO) {
      val movies = collectionsRepository.loadCollectionItems(collectionId)
      movies
        .mapIndexed { index, movie ->
          async {
            MovieDetailsCollectionItem.MovieItem(
              rank = index + 1,
              movie = movie,
              image = imagesProvider.findCachedImage(movie, POSTER),
              isMyMovie = myMoviesRepository.exists(movie.ids.media),
              isWatchlist = watchlistMoviesRepository.exists(movie.ids.media),
              translation = loadTranslation(movie, language),
              spoilers = settingsSpoilersRepository.getAll(),
              isLoading = false,
            )
          }
        }.awaitAll()
    }

  private suspend fun loadTranslation(
    movie: Movie,
    language: String,
  ): Translation? {
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(
      movie = movie,
      language = language,
      onlyLocal = true,
    )
  }
}
