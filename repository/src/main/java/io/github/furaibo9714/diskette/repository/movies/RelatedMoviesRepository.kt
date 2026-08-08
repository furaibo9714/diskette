package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.RelatedMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.IdTrakt
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
    val related = localSource.relatedMovies.getAllById(movie.ids.trakt.id)
    val latest = related.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedIds = related.map { it.idTrakt }
      return localSource.movies
        .getAll(relatedIds)
        .map { mappers.movie.fromDatabase(it) }
    }

    val remote = remoteSource.media
      .fetchRelatedMovies(movie.ids.trakt.id, min(0, 15), movie.ids.tmdb.id)
      .map { mappers.movie.fromNetwork(it) }

    cacheRelated(remote, movie.ids.trakt)

    return remote
  }

  private suspend fun cacheRelated(
    movies: List<Movie>,
    movieId: IdTrakt,
  ) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.movies.upsert(movies.map { mappers.movie.toDatabase(it) })
      localSource.relatedMovies.deleteById(movieId.id)
      localSource.relatedMovies.insert(
        movies.map {
          RelatedMovie.fromTraktId(it.ids.trakt.id, movieId.id, timestamp)
        },
      )
    }
  }
}
