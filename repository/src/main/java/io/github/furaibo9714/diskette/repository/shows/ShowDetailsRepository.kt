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

    /**
     * TMDB is the only metadata provider, so an item it doesn't know - a Floppy manual entry -
     * has nothing to refresh from, and what the import wrote is all there is. Returning early
     * also keeps the incomplete check below from re-fetching forever: a manual entry's runtime
     * is always -1, so it always looks incomplete.
     */
    val tmdbId = localShow?.idTmdb?.takeIf { it > 0 } ?: mediaId.tmdbIdOrNull
    if (tmdbId == null) {
      val cached = localShow ?: error("No local row and no TMDB id for $mediaId. Nothing to load.")
      return mappers.show.fromDatabase(cached)
    }

    val isIncomplete = localShow != null && localShow.runtime <= 0
    if (force || localShow == null || isIncomplete || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = remoteSource.media.fetchShow(tmdbId)
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
}
