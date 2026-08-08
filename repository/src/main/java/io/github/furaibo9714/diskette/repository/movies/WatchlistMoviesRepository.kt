package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
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

  suspend fun loadAllIds() = localSource.watchlistMovies.getAllMediaIds()

  suspend fun load(id: MediaId) =
    localSource.watchlistMovies.getById(id.key)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun insert(id: MediaId) {
    val movie = WatchlistMovie.fromMediaId(id.key, nowUtcMillis())
    transactions.withTransaction {
      with(localSource) {
        watchlistMovies.insert(movie)
        myMovies.deleteById(movie.mediaId)
        archiveMovies.deleteById(movie.mediaId)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: MediaId) {
    localSource.watchlistMovies.deleteById(id.key)
    cache.invalidate()
  }

  suspend fun exists(id: MediaId) = localSource.watchlistMovies.checkExists(id.key)
}
