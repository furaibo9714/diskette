package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.MoviesSyncLog
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import javax.inject.Inject

class MovieDetailsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun load(
    mediaId: MediaId,
    force: Boolean = false,
  ): Movie {
    val local = localSource.movies.getById(mediaId.key)

    /**
     * TMDB is the only metadata provider, so an item it doesn't know - a Floppy manual entry -
     * has nothing to refresh from, and what the import wrote is all there is. Returning early
     * also keeps the incomplete check below from re-fetching forever: a manual entry's runtime
     * is always -1, so it always looks incomplete.
     */
    val tmdbId = local?.idTmdb?.takeIf { it > 0 } ?: mediaId.tmdbIdOrNull
    if (tmdbId == null) {
      val cached = local ?: error("No local row and no TMDB id for $mediaId. Nothing to load.")
      return mappers.movie.fromDatabase(cached)
    }

    val isIncomplete = local != null && local.runtime <= 0
    if (force || local == null || isIncomplete || nowUtcMillis() - local.updatedAt > Config.MOVIE_DETAILS_CACHE_DURATION) {
      val remote = remoteSource.media.fetchMovie(tmdbId)
      val movie = mappers.movie.fromNetwork(remote)
      localSource.movies.upsert(listOf(mappers.movie.toDatabase(movie)))
      localSource.moviesSyncLog.upsert(MoviesSyncLog(movie.mediaId.key, nowUtcMillis()))
      return movie
    }
    return mappers.movie.fromDatabase(local)
  }

  suspend fun find(idImdb: IdImdb): Movie? {
    val localMovie = localSource.movies.getById(idImdb.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun find(idTmdb: IdTmdb): Movie? {
    val localMovie = localSource.movies.getByTmdbId(idTmdb.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun find(idSlug: IdSlug): Movie? {
    val localMovie = localSource.movies.getBySlug(idSlug.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun delete(mediaId: MediaId) = localSource.movies.deleteById(mediaId.key)
}
