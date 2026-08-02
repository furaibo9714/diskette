package com.michaldrabik.repository.shows

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.WatchlistShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class WatchlistShowsRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: ShowsCollectionCache,
) {

  suspend fun loadAll(): List<Show> {
    cache.watchlistShows?.let { return it }
    val shows = localSource.watchlistShows
      .getAll()
      .map { mappers.show.fromDatabase(it) }
    cache.watchlistShows = shows
    return shows
  }

  suspend fun loadAllIds() = localSource.watchlistShows.getAllTraktIds()

  suspend fun load(id: IdTrakt) =
    localSource.watchlistShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun insert(id: IdTrakt) {
    val dbShow = WatchlistShow.fromTraktId(id.id, nowUtcMillis())
    with(localSource) {
      transactions.withTransaction {
        watchlistShows.insert(dbShow)
        myShows.deleteById(id.id)
        archiveShows.deleteById(id.id)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: IdTrakt) {
    localSource.watchlistShows.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: IdTrakt) = localSource.watchlistShows.checkExists(id.id)
}
