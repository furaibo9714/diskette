package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbShowDetails(
  val id: Long?,
  val name: String?,
  val original_name: String?,
  val overview: String?,
  val first_air_date: String?,
  val episode_run_time: List<Int>?,
  val genres: List<TmdbGenre>?,
  val networks: List<TmdbNetwork>?,
  val origin_country: List<String>?,
  val status: String?,
  val vote_average: Float?,
  val vote_count: Long?,
  val homepage: String?,
  val number_of_episodes: Int?,
  val videos: TmdbVideosResponse?,
  val content_ratings: TmdbContentRatingsResponse?,
  val seasons: List<TmdbSeasonSummary>?,
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonSummary(
  val season_number: Int?,
  val episode_count: Int?,
  val name: String?,
  val air_date: String?,
)

@JsonClass(generateAdapter = true)
data class TmdbNetwork(
  val id: Int?,
  val name: String?,
)

@JsonClass(generateAdapter = true)
data class TmdbContentRatingsResponse(
  val results: List<TmdbContentRating>?,
)

@JsonClass(generateAdapter = true)
data class TmdbContentRating(
  val iso_3166_1: String?,
  val rating: String?,
)
