package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
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

  suspend fun loadAllIds() = localSource.watchlistShows.getAllMediaIds()

  suspend fun load(id: MediaId) =
    localSource.watchlistShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun insert(id: MediaId) {
    val dbShow = WatchlistShow.fromMediaId(id.id, nowUtcMillis())
    with(localSource) {
      transactions.withTransaction {
        watchlistShows.insert(dbShow)
        myShows.deleteById(id.id)
        archiveShows.deleteById(id.id)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: MediaId) {
    localSource.watchlistShows.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: MediaId) = localSource.watchlistShows.checkExists(id.id)
}
