package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbSeasonDetails(
  val id: Long?,
  val air_date: String?,
  val name: String?,
  val overview: String?,
  val season_number: Int?,
  val episodes: List<TmdbEpisodeDetails>?,
)

@JsonClass(generateAdapter = true)
data class TmdbEpisodeDetails(
  val id: Long?,
  val name: String?,
  val overview: String?,
  val vote_average: Float?,
  val vote_count: Int?,
  val air_date: String?,
  val episode_number: Int?,
  val season_number: Int?,
  val runtime: Int?,
)
