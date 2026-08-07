package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Movie
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
