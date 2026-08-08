package io.github.furaibo9714.diskette.ui_base.floppy.imports

import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_TV
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaDetail
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaItemRef
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.MediaSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import io.github.furaibo9714.diskette.data_local.database.model.Movie as MovieDb
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb

/**
 * Turns a Floppy media item reference into a local row, creating a thin placeholder when the item
 * isn't cached yet. A Floppy reference is already a (source, media_id) pair, which is exactly what
 * [MediaId] is, so resolving one is a direct read of the local row rather than a lookup through
 * some other id.
 *
 * Metadata comes from Floppy's own media-detail endpoint
 * ([FloppyService.getMediaDetail][io.github.furaibo9714.diskette.data_remote.floppy.api.FloppyService.getMediaDetail]),
 * which proxies the underlying provider through Floppy's backend and cache, so no TMDB round trip
 * happens here. Manual entries have no provider to proxy, so they stay title-only. If the call
 * fails, a bare row built from the tracked-list item's `title` keeps the import from dropping the
 * item entirely.
 */
@Singleton
class FloppyManualMediaResolver @Inject constructor(
  private val localSource: LocalDataSource,
  private val connectionManager: FloppyConnectionManager,
) {

  suspend fun resolveShowId(item: FloppyMediaItemRef): MediaId? {
    val mediaId = toMediaId(item) ?: return null
    if (localSource.shows.getById(mediaId.key) != null) return mediaId

    val detail = fetchDetail(MEDIA_TYPE_TV, item)
    val title = resolveTitle(detail, item) ?: return null
    localSource.shows.upsert(listOf(buildShow(mediaId, title, detail)))
    return mediaId
  }

  suspend fun resolveMovieId(item: FloppyMediaItemRef): MediaId? {
    val mediaId = toMediaId(item) ?: return null
    if (localSource.movies.getById(mediaId.key) != null) return mediaId

    val detail = fetchDetail(MEDIA_TYPE_MOVIE, item)
    val title = resolveTitle(detail, item) ?: return null
    localSource.movies.upsert(listOf(buildMovie(mediaId, title, detail)))
    return mediaId
  }

  /**
   * An unrecognised source can't be addressed back to Floppy and has no provider the app knows how
   * to fetch from, so it is skipped rather than guessed at.
   */
  private fun toMediaId(item: FloppyMediaItemRef): MediaId? {
    val source = MediaSource.fromKeyOrNull(item.source)
    if (source == null) {
      Timber.w("Unknown Floppy media source '${item.source}'. Skipping import of this item.")
      return null
    }
    return MediaId(source, item.mediaId).takeIf { !it.isEmpty }
  }

  private fun resolveTitle(
    detail: FloppyMediaDetail?,
    item: FloppyMediaItemRef,
  ): String? = detail?.title?.takeIf { it.isNotBlank() } ?: item.title?.takeIf { it.isNotBlank() }

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

  private fun buildShow(
    mediaId: MediaId,
    title: String,
    detail: FloppyMediaDetail?,
  ) = ShowDb(
    mediaId = mediaId.key,
    idTvdb = -1,
    idTmdb = mediaId.tmdbIdOrNull ?: -1,
    idImdb = "",
    idSlug = "",
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
    runtimeMax = -1,
  )

  private fun buildMovie(
    mediaId: MediaId,
    title: String,
    detail: FloppyMediaDetail?,
  ) = MovieDb(
    mediaId = mediaId.key,
    idTmdb = mediaId.tmdbIdOrNull ?: -1,
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
}
