package com.michaldrabik.repository.movies.ratings

import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.SOURCE_TMDB
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Ratings
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
