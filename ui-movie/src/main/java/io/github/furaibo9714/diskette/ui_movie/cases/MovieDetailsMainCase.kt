package io.github.furaibo9714.diskette.ui_movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
) {

  suspend fun loadDetails(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      moviesRepository.movieDetails.load(mediaId)
    }

  suspend fun removeMalformedMovie(mediaId: MediaId) {
    withContext(dispatchers.IO) {
      with(moviesRepository) {
        myMovies.delete(mediaId)
        watchlistMovies.delete(mediaId)
        movieDetails.delete(mediaId)
      }
    }
    Timber.d("Removing malformed movie...")
  }
}
