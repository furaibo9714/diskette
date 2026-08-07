package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

/**
 * Response of TMDB's `/find/{external_id}` lookup. Results are split per media type rather than
 * carrying a `media_type` discriminator, so entries here arrive without one set.
 */
@JsonClass(generateAdapter = true)
data class TmdbFindResponse(
  val movie_results: List<TmdbSearchResultItem>?,
  val tv_results: List<TmdbSearchResultItem>?,
)
