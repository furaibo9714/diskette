package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.common.extensions.toUtcZone
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.MyMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import java.time.ZonedDateTime
import javax.inject.Inject

class MyMoviesRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: MoviesCollectionCache,
) {

  suspend fun load(id: MediaId) =
    localSource.myMovies.getById(id.key)?.let {
      mappers.movie.fromDatabase(it)
    }

  suspend fun loadAll(): List<Movie> {
    cache.myMovies?.let { return it }
    val movies = localSource.myMovies
      .getAll()
      .map { mappers.movie.fromDatabase(it) }
    cache.myMovies = movies
    return movies
  }

  suspend fun loadAll(ids: List<MediaId>) =
    localSource.myMovies
      .getAll(ids.map { it.key })
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    localSource.myMovies
      .getAllRecent(amount)
      .map { mappers.movie.fromDatabase(it) }

  suspend fun loadAllIds() = localSource.myMovies.getAllMediaIds()

  suspend fun insert(
    id: MediaId,
    customDate: ZonedDateTime?,
  ) {
    val movie = MyMovie.fromMediaId(
      mediaId = id.key,
      timestamp = customDate?.toUtcZone()?.toMillis() ?: nowUtcMillis(),
    )
    transactions.withTransaction {
      with(localSource) {
        myMovies.insert(listOf(movie))
        watchlistMovies.deleteById(id.key)
        archiveMovies.deleteById(id.key)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: MediaId) {
    localSource.myMovies.deleteById(id.key)
    cache.invalidate()
  }

  suspend fun exists(id: MediaId) = localSource.myMovies.checkExists(id.key)
}
