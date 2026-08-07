package io.github.furaibo9714.diskette.repository.movies.ratings

import io.github.furaibo9714.diskette.common.ConfigVariant
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.SOURCE_TMDB
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Ratings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesExternalRatingsRepository @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadRatings(movie: Movie): Ratings {
    val localRatings = localSource.movieRatings.getById(movie.traktId)
    localRatings?.let {
      if (nowUtcMillis() - it.updatedAt < ConfigVariant.RATINGS_CACHE_DURATION) {
        return mappers.ratings.fromDatabase(it)
      }
    }

    if (!connectionManager.isConfigured() || movie.ids.tmdb.id <= 0) {
      return Ratings()
    }

    val mediaDetail = connectionManager.service().getMediaDetail(MEDIA_TYPE_MOVIE, SOURCE_TMDB, movie.ids.tmdb.id.toString())
    val remoteRatings = mappers.ratings.fromNetwork(mediaDetail)

    val dbRatings = mappers.ratings.toMovieDatabase(movie.ids.trakt, remoteRatings)
    localSource.movieRatings.upsert(dbRatings)

    return remoteRatings
  }
}
