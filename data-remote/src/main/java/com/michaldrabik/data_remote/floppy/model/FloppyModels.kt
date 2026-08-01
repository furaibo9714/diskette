package com.michaldrabik.data_remote.floppy.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Media is addressed statelessly as (media_type, source, media_id) - e.g. "movie/tmdb/27205" -
 * rather than by an opaque Floppy-side id, so these DTOs only carry the fields Showly needs
 * (Moshi ignores the large amount of unrelated metadata Floppy returns per item).
 */
@JsonClass(generateAdapter = true)
data class FloppyMediaItemRef(
  @Json(name = "media_id") val mediaId: String,
  @Json(name = "source") val source: String,
  @Json(name = "media_type") val mediaType: String,
  @Json(name = "title") val title: String? = null,
  @Json(name = "season_number") val seasonNumber: Int? = null,
  @Json(name = "episode_number") val episodeNumber: Int? = null,
)

/**
 * status: 0 Planning, 1 In progress, 2 Paused, 3 Completed, 4 Dropped.
 * status == 0 is Showly's "watchlist" state; status >= 3 (Completed) is "watched".
 */
@JsonClass(generateAdapter = true)
data class FloppyMedia(
  @Json(name = "id") val id: Long,
  @Json(name = "status") val status: Int?,
  @Json(name = "item") val item: FloppyMediaItemRef,
)

@JsonClass(generateAdapter = true)
data class FloppyPagination(
  @Json(name = "total") val total: Int,
  @Json(name = "limit") val limit: Int,
  @Json(name = "offset") val offset: Int,
  @Json(name = "next") val next: String?,
)

@JsonClass(generateAdapter = true)
data class FloppyMediaListResponse(
  @Json(name = "pagination") val pagination: FloppyPagination,
  @Json(name = "results") val results: List<FloppyMedia>,
)

@JsonClass(generateAdapter = true)
data class FloppyTrackRequest(
  @Json(name = "source") val source: String,
  @Json(name = "media_id") val mediaId: String,
)

@JsonClass(generateAdapter = true)
data class FloppyStatusUpdateRequest(
  @Json(name = "status") val status: String,
)

@JsonClass(generateAdapter = true)
data class FloppyInfo(
  @Json(name = "version") val version: String,
)
