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
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject

class ShowDetailsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun load(
    mediaId: MediaId,
    force: Boolean = false,
  ): Show {
    val localShow = localSource.shows.getById(mediaId.key)
    val isIncomplete = localShow != null && localShow.runtime <= 0
    if (force || localShow == null || isIncomplete || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = remoteSource.media.fetchShow(requireTmdbId(mediaId, localShow?.idTmdb))
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

  suspend fun delete(mediaId: MediaId) {
    with(localSource) {
      transactions.withTransaction {
        shows.deleteById(mediaId.key)
        seasons.deleteAllForShow(mediaId.key)
        episodes.deleteAllForShow(mediaId.key)
      }
    }
  }

  /**
   * TMDB is the only metadata provider, so a non-TMDB item (a Floppy manual entry) has nothing to
   * fetch. The cached row's TMDB id wins when present, since it survives even for items whose
   * identity is not TMDB-based.
   */
  private fun requireTmdbId(
    mediaId: MediaId,
    cachedTmdbId: Long?,
  ): Long =
    cachedTmdbId?.takeIf { it > 0 }
      ?: mediaId.tmdbIdOrNull
      ?: error("No TMDB id for $mediaId. Cannot fetch details.")
}
