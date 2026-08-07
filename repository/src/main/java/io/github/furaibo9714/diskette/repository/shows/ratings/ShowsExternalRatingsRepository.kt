package io.github.furaibo9714.diskette.repository.shows.ratings

import io.github.furaibo9714.diskette.common.ConfigVariant
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.SOURCE_TMDB
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Ratings
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsExternalRatingsRepository @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadRatings(show: Show): Ratings {
    val localRatings = localSource.showRatings.getById(show.traktId)
    localRatings?.let {
      if (nowUtcMillis() - it.updatedAt < ConfigVariant.RATINGS_CACHE_DURATION) {
        return mappers.ratings.fromDatabase(it)
      }
    }

    if (!connectionManager.isConfigured() || show.ids.tmdb.id <= 0) {
      return Ratings()
    }

    val mediaDetail = connectionManager.service().getMediaDetail(MEDIA_TYPE_TV, SOURCE_TMDB, show.ids.tmdb.id.toString())
    val remoteRatings = mappers.ratings.fromNetwork(mediaDetail)

    val dbRatings = mappers.ratings.toShowDatabase(show.ids.trakt, remoteRatings)
    localSource.showRatings.upsert(dbRatings)

    return remoteRatings
  }
}
