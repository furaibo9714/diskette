package io.github.furaibo9714.diskette.ui_movie.sections.related.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsRelatedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
) {

  // TODO Add Hidden items
  suspend fun loadRelatedMovies(movie: Movie): List<Movie> =
    withContext(dispatchers.IO) {
      moviesRepository.relatedMovies
        .loadAll(movie)
        .sortedWith(compareBy({ it.votes }, { it.rating }))
        .reversed()
    }
}
