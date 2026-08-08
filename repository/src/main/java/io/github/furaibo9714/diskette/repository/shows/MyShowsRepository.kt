package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.model.MyShow
import io.github.furaibo9714.diskette.data_local.sources.ArchiveShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MyShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.WatchlistShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject

class MyShowsRepository @Inject constructor(
  private val myShowsLocalSource: MyShowsLocalDataSource,
  private val watchlistShowsLocalSource: WatchlistShowsLocalDataSource,
  private val hiddenShowsLocalDataSource: ArchiveShowsLocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: ShowsCollectionCache,
) {

  suspend fun load(id: MediaId) =
    myShowsLocalSource.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAll(): List<Show> {
    cache.myShows?.let { return it }
    val shows = myShowsLocalSource
      .getAll()
      .map { mappers.show.fromDatabase(it) }
    cache.myShows = shows
    return shows
  }

  suspend fun loadAll(ids: List<MediaId>) =
    myShowsLocalSource
      .getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllRecent(amount: Int) =
    myShowsLocalSource
      .getAllRecent(amount)
      .map { mappers.show.fromDatabase(it) }

  suspend fun loadAllIds() = myShowsLocalSource.getAllMediaIds()

  suspend fun insert(
    id: MediaId,
    lastWatchedAt: Long,
  ) {
    val nowUtc = nowUtcMillis()
    val dbShow = MyShow.fromMediaId(
      mediaId = id.id,
      createdAt = nowUtc,
      updatedAt = nowUtc,
      watchedAt = lastWatchedAt,
    )
    transactions.withTransaction {
      myShowsLocalSource.insert(listOf(dbShow))
      watchlistShowsLocalSource.deleteById(id.id)
      hiddenShowsLocalDataSource.deleteById(id.id)
    }
    cache.invalidate()
  }

  suspend fun delete(id: MediaId) {
    myShowsLocalSource.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: MediaId) = myShowsLocalSource.checkExists(id.id)

  suspend fun updateWatchedAt(
    mediaId: MediaId,
    watchedAt: Long,
  ) {
    myShowsLocalSource.updateWatchedAt(mediaId.key, watchedAt)
    cache.invalidate()
  }
}
