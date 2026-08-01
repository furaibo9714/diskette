package com.michaldrabik.ui_base.floppy.imports

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.floppy.model.FloppyMediaItemRef
import com.michaldrabik.ui_base.floppy.FloppyManualSyntheticIds
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_local.database.model.Movie as MovieDb
import com.michaldrabik.data_local.database.model.Show as ShowDb

/**
 * Resolves a Floppy media item reference to a local [IdTrakt], for both TMDB-backed items
 * (matched by tmdb id, must already exist locally) and Floppy "manual" items (no external
 * provider id - a thin local Show/Movie row is created on first encounter, keyed by a synthetic
 * id derived from Floppy's UUID; see [FloppyManualSyntheticIds]).
 */
@Singleton
class FloppyManualMediaResolver @Inject constructor(
  private val localSource: LocalDataSource,
) {

  suspend fun resolveShowId(item: FloppyMediaItemRef): IdTrakt? {
    val tmdbId = item.mediaId.toLongOrNull()
    if (tmdbId != null) {
      val show = localSource.shows.getByTmdbId(tmdbId) ?: return null
      return IdTrakt(show.idTrakt)
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
      val movie = localSource.movies.getByTmdbId(tmdbId) ?: return null
      return IdTrakt(movie.idTrakt)
    }
    if (item.source != SOURCE_MANUAL) return null
    val title = item.title?.takeIf { it.isNotBlank() } ?: return null
    val traktId = FloppyManualSyntheticIds.toSyntheticTraktId(item.mediaId)
    if (localSource.movies.getById(traktId) == null) {
      localSource.movies.upsert(listOf(buildManualMovie(traktId, item.mediaId, title)))
    }
    return IdTrakt(traktId)
  }

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
