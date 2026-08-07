package io.github.furaibo9714.diskette.ui_movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    withContext(dispatchers.IO) {
      moviesRepository.movieDetails.load(idTrakt)
    }

  suspend fun removeMalformedMovie(idTrakt: IdTrakt) {
    withContext(dispatchers.IO) {
      with(moviesRepository) {
        myMovies.delete(idTrakt)
        watchlistMovies.delete(idTrakt)
        movieDetails.delete(idTrakt)
      }
    }
    Timber.d("Removing malformed movie...")
  }
}
