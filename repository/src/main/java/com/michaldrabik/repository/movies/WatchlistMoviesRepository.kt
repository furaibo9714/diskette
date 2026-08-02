package com.michaldrabik.repository.movies

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject

class WatchlistMoviesRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: MoviesCollectionCache,
) {

  suspend fun loadAll(): List<Movie> {
    cache.watchlistMovies?.let { return it }
    val movies = localSource.watchlistMovies
      .getAll()
      .map { mappers.movie.fromDatabase(it) }
    cache.watchlistMovies = movies
    return movies
  }

  suspend fun loadAllIds() = localSource.watchlistMovies.getAllTraktIds()

  suspend fun load(id: IdTrakt) =
    localSource.watchlistMovies.getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val movie = WatchlistMovie.fromTraktId(id.id, nowUtcMillis())
    transactions.withTransaction {
      with(localSource) {
        watchlistMovies.insert(movie)
        myMovies.deleteById(movie.idTrakt)
        archiveMovies.deleteById(movie.idTrakt)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: IdTrakt) {
    localSource.watchlistMovies.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: IdTrakt) = localSource.watchlistMovies.checkExists(id.id)
}
