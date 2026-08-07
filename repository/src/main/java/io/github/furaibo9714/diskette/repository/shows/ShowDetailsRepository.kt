package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject

class ShowDetailsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun load(
    idTrakt: IdTrakt,
    force: Boolean = false,
  ): Show {
    val localShow = localSource.shows.getById(idTrakt.id)
    val isIncomplete = localShow != null && localShow.runtime <= 0
    if (force || localShow == null || isIncomplete || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = remoteSource.trakt.fetchShow(idTrakt.id, localShow?.idTmdb)
      val show = mappers.show.fromNetwork(remoteShow)
      localSource.shows.upsert(listOf(mappers.show.toDatabase(show)))
      return show
    }
    return mappers.show.fromDatabase(localShow)
  }

  suspend fun find(idImdb: IdImdb): Show? {
    val localShow = localSource.shows.getById(idImdb.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun find(idTmdb: IdTmdb): Show? {
    val localShow = localSource.shows.getByTmdbId(idTmdb.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun find(idSlug: IdSlug): Show? {
    val localShow = localSource.shows.getBySlug(idSlug.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun delete(idTrakt: IdTrakt) {
    with(localSource) {
      transactions.withTransaction {
        shows.deleteById(idTrakt.id)
        seasons.deleteAllForShow(idTrakt.id)
        episodes.deleteAllForShow(idTrakt.id)
      }
    }
  }
}
