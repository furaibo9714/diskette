package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuLoadItemCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadItem(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val movie = moviesRepository.movieDetails.load(mediaId)
      val dateFormat = dateFormatProvider.loadShortDayFormat()
      val language = translationsRepository.getLanguage()
      val spoilers = settingsSpoilersRepository.getAll()

      val imageAsync = async { imagesProvider.loadRemoteImage(movie, ImageType.POSTER) }
      val translationAsync =
        async { translationsRepository.loadTranslation(movie, language = language, onlyLocal = true) }
      val ratingAsync = async { ratingsRepository.movies.loadRatings(listOf(movie)) }

      val isMyMovieAsync = async { moviesRepository.myMovies.exists(mediaId) }
      val isWatchlistAsync = async { moviesRepository.watchlistMovies.exists(mediaId) }
      val isHiddenAsync = async { moviesRepository.hiddenMovies.exists(mediaId) }

      val isPinnedAsync = async { pinnedItemsRepository.isItemPinned(movie) }

      MovieContextItem(
        movie = movie,
        image = imageAsync.await(),
        translation = translationAsync.await(),
        userRating = ratingAsync.await().firstOrNull()?.rating,
        isMyMovie = isMyMovieAsync.await(),
        isWatchlist = isWatchlistAsync.await(),
        isHidden = isHiddenAsync.await(),
        isPinnedTop = isPinnedAsync.await(),
        dateFormat = dateFormat,
        spoilers = spoilers,
      )
    }
}
