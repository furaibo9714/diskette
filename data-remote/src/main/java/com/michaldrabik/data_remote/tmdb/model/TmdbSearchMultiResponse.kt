package com.michaldrabik.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbSearchMultiResponse(
  val page: Int?,
  val total_pages: Int?,
  val total_results: Int?,
  val results: List<TmdbSearchResultItem>?,
)

/**
 * TMDB's `/search/multi` returns movie/tv/person results as one flat shape distinguished by
 * `media_type` - not a discriminated JSON union, so all fields are nullable here regardless of
 * which type a given result actually is.
 */
@JsonClass(generateAdapter = true)
data class TmdbSearchResultItem(
  val id: Long?,
  val media_type: String?,
  val title: String?,
  val original_title: String?,
  val name: String?,
  val original_name: String?,
  val release_date: String?,
  val first_air_date: String?,
  val overview: String?,
  val poster_path: String?,
  val backdrop_path: String?,
  val vote_average: Float?,
  val vote_count: Long?,
  val genre_ids: List<Int>?,
  val popularity: Float?,
  val original_language: String?,
)
