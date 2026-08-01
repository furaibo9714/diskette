package com.michaldrabik.ui_base.floppy.imports

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import com.michaldrabik.data_remote.floppy.model.FloppyMediaDetail
import com.michaldrabik.data_remote.floppy.model.FloppyMediaItemRef
import com.michaldrabik.data_remote.tmdb.TmdbSyntheticIds
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.ui_base.floppy.FloppyManualSyntheticIds
import com.michaldrabik.ui_model.IdTrakt
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_local.database.model.Movie as MovieDb
import com.michaldrabik.data_local.database.model.Show as ShowDb

/**
 * Resolves a Floppy media item reference to a local [IdTrakt], for both TMDB-backed items and
 * Floppy "manual" items (no external provider id), without ever calling TMDB directly - Floppy
 * is the sole metadata source for this fork. TMDB-backed rows are enriched via Floppy's own
 * media-detail endpoint ([FloppyService.getMediaDetail][com.michaldrabik.data_remote.floppy.api.FloppyService.getMediaDetail]),
 * which proxies the underlying provider's metadata through Floppy's own backend (and its cache -
 * confirmed live: an already-tracked item's detail response is a fast, fully-populated payload,
 * not a fresh third-party fetch) - title/overview/rating/year all come from that single call, no
 * TMDB round trip from Showly. If that call fails for any reason, falls back to a bare row built
 * from just the tracked-list item's `title` so import never silently drops the item entirely.
 *
 * TMDB-backed rows are keyed by a synthetic id derived from the real tmdb id (see
 * [TmdbSyntheticIds], the same scheme Discover/search-sourced content uses, so a later browse to
 * the same title resolves to the same row); manual rows are keyed by a synthetic id derived from
 * Floppy's UUID (see [FloppyManualSyntheticIds]) and stay title-only, since Floppy has no
 * provider to proxy metadata from for those.
 */
@Singleton
class FloppyManualMediaResolver @Inject constructor(
  private val localSource: LocalDataSource,
  private val connectionManager: FloppyConnectionManager,
) {

  suspend fun resolveShowId(item: FloppyMediaItemRef): IdTrakt? {
    val tmdbId = item.mediaId.toLongOrNull()
    if (tmdbId != null) {
      val localShow = localSource.shows.getByTmdbId(tmdbId)
      if (localShow != null) return IdTrakt(localShow.idTrakt)

      val detail = fetchDetail(MEDIA_TYPE_TV, item)
      val title = detail?.title?.takeIf { it.isNotBlank() }
        ?: item.title?.takeIf { it.isNotBlank() }
        ?: return null

      val traktId = TmdbSyntheticIds.toSyntheticTraktId(tmdbId)
      if (localSource.shows.getById(traktId) == null) {
        localSource.shows.upsert(listOf(buildTmdbShow(traktId, tmdbId, title, detail)))
      }
      return IdTrakt(traktId)
    }
    if (item.source != SOURCE_MANUAL) return null
    val title = item.title?.takeIf { it.isNotBlank() } ?: return null
    val traktId = FloppyManualSyntheticIds.toSyntheticTraktId(item.mediaId)
    if (localSource.shows.getById(traktId) == null) {
      localSource.shows.upsert(listOf(buildManualShow(traktId, item.mediaId, title)))
    }
    return IdTrakt(traktId)
  }

  suspend fun resolveMovieId(item: FloppyMediaItemRef): IdTrakt? {
    val tmdbId = item.mediaId.toLongOrNull()
    if (tmdbId != null) {
      val localMovie = localSource.movies.getByTmdbId(tmdbId)
      if (localMovie != null) return IdTrakt(localMovie.idTrakt)

      val detail = fetchDetail(MEDIA_TYPE_MOVIE, item)
      val title = detail?.title?.takeIf { it.isNotBlank() }
        ?: item.title?.takeIf { it.isNotBlank() }
        ?: return null

      val traktId = TmdbSyntheticIds.toSyntheticTraktId(tmdbId)
      if (localSource.movies.getById(traktId) == null) {
        localSource.movies.upsert(listOf(buildTmdbMovie(traktId, tmdbId, title, detail)))
      }
      return IdTrakt(traktId)
    }
    if (item.source != SOURCE_MANUAL) return null
    val title = item.title?.takeIf { it.isNotBlank() } ?: return null
    val traktId = FloppyManualSyntheticIds.toSyntheticTraktId(item.mediaId)
    if (localSource.movies.getById(traktId) == null) {
      localSource.movies.upsert(listOf(buildManualMovie(traktId, item.mediaId, title)))
    }
    return IdTrakt(traktId)
  }

  private suspend fun fetchDetail(
    mediaType: String,
    item: FloppyMediaItemRef,
  ): FloppyMediaDetail? {
    if (!connectionManager.isConfigured()) return null
    return try {
      connectionManager.service().getMediaDetail(mediaType, item.source, item.mediaId)
    } catch (error: Throwable) {
      Timber.w(error, "Failed to fetch Floppy media detail for ${item.source}/${item.mediaId}.")
      null
    }
  }

  private fun buildTmdbShow(
    traktId: Long,
    tmdbId: Long,
    title: String,
    detail: FloppyMediaDetail?,
  ) = ShowDb(
    idTrakt = traktId,
    idTvdb = -1,
    idTmdb = tmdbId,
    idImdb = "",
    idSlug = "",
    idTvrage = -1,
    title = title,
    year = parseYear(detail?.details?.firstAirDate),
    overview = detail?.overview ?: "",
    firstAired = "",
    runtime = -1,
    airtimeDay = "",
    airtimeTime = "",
    airtimeTimezone = "",
    certification = "",
    network = "",
    country = "",
    trailer = "",
    homepage = "",
    status = "",
    rating = detail?.score?.toFloat() ?: -1F,
    votes = detail?.scoreCount?.toLong() ?: -1,
    commentCount = -1,
    genres = "",
    airedEpisodes = -1,
    createdAt = -1,
    updatedAt = -1,
  )

  private fun buildTmdbMovie(
    traktId: Long,
    tmdbId: Long,
    title: String,
    detail: FloppyMediaDetail?,
  ) = MovieDb(
    idTrakt = traktId,
    idTmdb = tmdbId,
    idImdb = "",
    idSlug = "",
    title = title,
    year = parseYear(detail?.details?.releaseDate),
    overview = detail?.overview ?: "",
    released = "",
    runtime = -1,
    country = "",
    trailer = "",
    language = "",
    homepage = "",
    status = "",
    rating = detail?.score?.toFloat() ?: -1F,
    votes = detail?.scoreCount?.toLong() ?: -1,
    commentCount = -1,
    genres = "",
    updatedAt = -1,
    createdAt = -1,
  )

  private fun parseYear(date: String?): Int = date?.take(4)?.toIntOrNull() ?: -1

  private fun buildManualShow(
    traktId: Long,
    floppyMediaId: String,
    title: String,
  ) = ShowDb(
    idTrakt = traktId,
    idTvdb = -1,
    idTmdb = -1,
    idImdb = "",
    idSlug = FloppyManualSyntheticIds.toSlugValue(floppyMediaId),
    idTvrage = -1,
    title = title,
    year = -1,
    overview = "",
    firstAired = "",
    runtime = -1,
    airtimeDay = "",
    airtimeTime = "",
    airtimeTimezone = "",
    certification = "",
    network = "",
    country = "",
    trailer = "",
    homepage = "",
    status = "",
    rating = -1F,
    votes = -1,
    commentCount = -1,
    genres = "",
    airedEpisodes = -1,
    createdAt = -1,
    updatedAt = -1,
  )

  private fun buildManualMovie(
    traktId: Long,
    floppyMediaId: String,
    title: String,
  ) = MovieDb(
    idTrakt = traktId,
    idTmdb = -1,
    idImdb = "",
    idSlug = FloppyManualSyntheticIds.toSlugValue(floppyMediaId),
    title = title,
    year = -1,
    overview = "",
    released = "",
    runtime = -1,
    country = "",
    trailer = "",
    language = "",
    homepage = "",
    status = "",
    rating = -1F,
    votes = -1,
    commentCount = -1,
    genres = "",
    updatedAt = -1,
    createdAt = -1,
  )

  companion object {
    private const val SOURCE_MANUAL = "manual"
  }
}
