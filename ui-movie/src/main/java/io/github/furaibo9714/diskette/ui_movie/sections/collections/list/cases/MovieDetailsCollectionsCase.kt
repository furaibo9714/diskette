package io.github.furaibo9714.diskette.ui_movie.sections.collections.list.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MovieCollectionsRepository
import io.github.furaibo9714.diskette.repository.movies.MovieCollectionsRepository.Source
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.MovieCollection
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val repository: MovieCollectionsRepository,
) {

  suspend fun loadMovieCollections(movie: Movie): Pair<List<MovieCollection>, Source> =
    withContext(dispatchers.IO) {
      try {
        val (collections, source) = repository.loadCollections(movie.ids.trakt)
        return@withContext Pair(
          collections.filter { it.itemCount != -1 },
          source,
        )
      } catch (error: Throwable) {
        return@withContext Pair(
          emptyList(),
          Source.LOCAL,
        )
      }
    }
}
