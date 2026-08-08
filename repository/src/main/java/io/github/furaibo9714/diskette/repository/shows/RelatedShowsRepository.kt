package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.RelatedShow
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import kotlin.math.min

class RelatedShowsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun loadAll(
    show: Show,
    hiddenCount: Int,
  ): List<Show> {
    val relatedShows = localSource.relatedShows.getAllById(show.mediaId.key)
    val latest = relatedShows.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedShowsIds = relatedShows.map { it.mediaId }
      return localSource.shows
        .getAll(relatedShowsIds)
        .map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = remoteSource.media
      .fetchRelatedShows(show.mediaId, min(hiddenCount, 10), show.ids.tmdb.id)
      .map { mappers.show.fromNetwork(it) }

    cacheRelatedShows(remoteShows, show.ids.media)

    return remoteShows
  }

  private suspend fun cacheRelatedShows(
    shows: List<Show>,
    showId: MediaId,
  ) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.shows.upsert(shows.map { mappers.show.toDatabase(it) })
      localSource.relatedShows.deleteById(showId.id)
      localSource.relatedShows.insert(
        shows.map {
          RelatedShow.fromMediaId(it.ids.media.id, showId.id, timestamp)
        },
      )
    }
  }
}
