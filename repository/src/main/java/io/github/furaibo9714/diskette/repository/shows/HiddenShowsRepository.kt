package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject

class HiddenShowsRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val cache: ShowsCollectionCache,
) {

  suspend fun loadAll(): List<Show> {
    cache.hiddenShows?.let { return it }
    val shows = localSource.archiveShows
      .getAll()
      .map { mappers.show.fromDatabase(it) }
    cache.hiddenShows = shows
    return shows
  }

  suspend fun loadAll(ids: List<MediaId>) =
    localSource.archiveShows
      .getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun load(id: MediaId) =
    localSource.archiveShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAllIds() = localSource.archiveShows.getAllMediaIds()

  suspend fun insert(id: MediaId) {
    val dbShow = ArchiveShow.fromMediaId(id.id, nowUtcMillis())
    with(localSource) {
      transactions.withTransaction {
        archiveShows.insert(dbShow)
        myShows.deleteById(id.id)
        watchlistShows.deleteById(id.id)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: MediaId) {
    localSource.archiveShows.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: MediaId) = localSource.archiveShows.getById(id.id) != null
}
