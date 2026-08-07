package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.IdTrakt
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

  suspend fun loadAll(ids: List<IdTrakt>) =
    localSource.archiveShows
      .getAll(ids.map { it.id })
      .map { mappers.show.fromDatabase(it) }

  suspend fun load(id: IdTrakt) =
    localSource.archiveShows.getById(id.id)?.let {
      mappers.show.fromDatabase(it)
    }

  suspend fun loadAllIds() = localSource.archiveShows.getAllTraktIds()

  suspend fun insert(id: IdTrakt) {
    val dbShow = ArchiveShow.fromTraktId(id.id, nowUtcMillis())
    with(localSource) {
      transactions.withTransaction {
        archiveShows.insert(dbShow)
        myShows.deleteById(id.id)
        watchlistShows.deleteById(id.id)
      }
    }
    cache.invalidate()
  }

  suspend fun delete(id: IdTrakt) {
    localSource.archiveShows.deleteById(id.id)
    cache.invalidate()
  }

  suspend fun exists(id: IdTrakt) = localSource.archiveShows.getById(id.id) != null
}
