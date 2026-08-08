package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.RelatedMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import javax.inject.Inject
import kotlin.math.min

class RelatedMoviesRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun loadAll(movie: Movie): List<Movie> {
    val related = localSource.relatedMovies.getAllById(movie.ids.media.key)
    val latest = related.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedIds = related.map { it.mediaId }
      return localSource.movies
        .getAll(relatedIds)
        .map { mappers.movie.fromDatabase(it) }
    }

    val remote = remoteSource.media
      .fetchRelatedMovies(movie.ids.tmdb.id, min(0, 15))
      .map { mappers.movie.fromNetwork(it) }

    cacheRelated(remote, movie.ids.media)

    return remote
  }

  private suspend fun cacheRelated(
    movies: List<Movie>,
    movieId: MediaId,
  ) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.movies.upsert(movies.map { mappers.movie.toDatabase(it) })
      localSource.relatedMovies.deleteById(movieId.id)
      localSource.relatedMovies.insert(
        movies.map {
          RelatedMovie.fromMediaId(it.ids.media.key, movieId.id, timestamp)
        },
      )
    }
  }
}
