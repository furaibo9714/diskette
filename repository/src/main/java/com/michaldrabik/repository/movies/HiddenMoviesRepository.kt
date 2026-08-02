package com.michaldrabik.repository.movies

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject

class HiddenMoviesRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: MoviesCollectionCache,
) {

  suspend fun loadAll(): List<Movie> {
    cache.hiddenMovies?.let { return it }
    val movies = localSource.archiveMovies
      .getAll()
      .map { mappers.movie.fromDatabase(it) }
    cache.hiddenMovies = movies
    return movies
  }

  suspend fun loadAll(ids: List<IdTrakt>) =
    localSource.archiveMovies
      .getAll(ids.map { it.id })
      .map { mappers.movie.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    localSource.archiveMovies.getById(id.id)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun loadAllIds() = localSource.archiveMovies.getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbMovie = ArchiveMovie.fromTraktId(id.id, nowUtcMillis())
    transactions.withTransaction {
      with(localSource) {
        archiveMovies.insert(dbMovie)
        myMovies.deleteById(id.id)
        watchlistMovies.deleteById(id.id)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: IdTrakt) {
    localSource.archiveMovies.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: IdTrakt) = localSource.archiveMovies.getById(id.id) != null
}
