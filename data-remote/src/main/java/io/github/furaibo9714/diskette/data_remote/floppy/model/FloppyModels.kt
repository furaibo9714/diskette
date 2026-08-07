package io.github.furaibo9714.diskette.data_remote.floppy.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Media is addressed statelessly as (media_type, source, media_id) - e.g. "movie/tmdb/27205" -
 * rather than by an opaque Floppy-side id, so these DTOs only carry the fields Diskette needs
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
 * status == 0 is Diskette's "watchlist" state; status >= 3 (Completed) is "watched".
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
data class FloppyScoreUpdateRequest(
  @Json(name = "score") val score: String?,
)

@JsonClass(generateAdapter = true)
data class FloppyInfo(
  @Json(name = "version") val version: String,
)

/**
 * Full media detail lookup - unlike [FloppyMedia] (the tracked-list item shape), this works for
 * any (media_type, source, media_id) regardless of whether the user is tracking it, since Floppy
 * proxies the underlying provider's metadata (e.g. TMDB) through its own backend and caches the
 * result - Diskette never calls the provider directly for this. Confirmed live against a real
 * Floppy instance: `synopsis`/`score`/`score_count`/`title`/`details.first_air_date`
 * (tv)/`details.release_date` (movie) are all real top-level fields, not guesses. Moshi ignores
 * the large amount of unrelated metadata Floppy returns per item.
 */
@JsonClass(generateAdapter = true)
data class FloppyMediaDetail(
  @Json(name = "id") val id: Long?,
  @Json(name = "title") val title: String?,
  @Json(name = "synopsis") val overview: String?,
  @Json(name = "genres") val genres: List<String>?,
  @Json(name = "score") val score: Double?,
  @Json(name = "score_count") val scoreCount: Int?,
  @Json(name = "details") val details: FloppyMediaDetailInfo?,
)

@JsonClass(generateAdapter = true)
data class FloppyMediaDetailInfo(
  @Json(name = "release_date") val releaseDate: String?,
  @Json(name = "first_air_date") val firstAirDate: String?,
)

@JsonClass(generateAdapter = true)
data class FloppyListCreateRequest(
  @Json(name = "name") val name: String,
  @Json(name = "description") val description: String?,
)

/** Only the field Diskette needs (the assigned list id) - Moshi ignores the rest. */
@JsonClass(generateAdapter = true)
data class FloppyList(
  @Json(name = "id") val id: Long,
)

/** Serializes to `{}` - some PUT endpoints (e.g. adding a media item to a list) require a body but have nothing to say. */
@JsonClass(generateAdapter = true)
class FloppyEmptyRequest

/**
 * `itemId` is Floppy's internal `Item` row id (the same [FloppyMediaDetail.id]) - not the
 * (media_type, source, media_id) triplet every other Floppy call in this codebase uses. Confirmed
 * by reading Floppy's actual source (`DiscoverHiddenView.post`); its own OpenAPI schema documents
 * no request body for this endpoint at all.
 */
@JsonClass(generateAdapter = true)
data class FloppyDiscoverHiddenRequest(
  @Json(name = "item_id") val itemId: Long,
  @Json(name = "action") val action: String,
)
