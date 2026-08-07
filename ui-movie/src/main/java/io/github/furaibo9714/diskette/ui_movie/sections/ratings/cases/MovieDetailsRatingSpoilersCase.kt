package io.github.furaibo9714.diskette.ui_movie.sections.ratings.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Ratings
import io.github.furaibo9714.diskette.ui_movie.MovieDetailsUiState.FollowedState
import io.github.furaibo9714.diskette.ui_movie.cases.MovieDetailsHiddenCase
import io.github.furaibo9714.diskette.ui_movie.cases.MovieDetailsMyMoviesCase
import io.github.furaibo9714.diskette.ui_movie.cases.MovieDetailsWatchlistCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsRatingSpoilersCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val myMoviesCase: MovieDetailsMyMoviesCase,
  private val watchlistCase: MovieDetailsWatchlistCase,
  private val hiddenCase: MovieDetailsHiddenCase,
  private val settingsSpoilersRepository: SettingsSpoilersRepository,
) {

  suspend fun hideSpoilerRatings(
    movie: Movie,
    ratings: Ratings,
  ): Ratings =
    withContext(dispatchers.IO) {
      val spoilers = settingsSpoilersRepository.getAll()

      val isMy = async { myMoviesCase.getMyMovie(movie) }
      val isWatchlist = async { watchlistCase.isWatchlist(movie) }
      val isHidden = async { hiddenCase.isHidden(movie) }

      val state = FollowedState(
        isMyMovie = isMy.await() != null,
        isWatchlist = isWatchlist.await(),
        isHidden = isHidden.await(),
        withAnimation = false,
      )

      val isMyHidden = spoilers.isMyMoviesRatingsHidden && state.isMyMovie
      val isWatchlistHidden = spoilers.isWatchlistMoviesRatingsHidden && state.isWatchlist
      val isHiddenHidden = spoilers.isHiddenMoviesRatingsHidden && state.isHidden
      val isNotCollectedHidden = spoilers.isNotCollectedMoviesRatingsHidden && !state.isInCollection()

      return@withContext ratings.copy(
        isHidden = isMyHidden || isWatchlistHidden || isHiddenHidden || isNotCollectedHidden,
      )
    }
}
