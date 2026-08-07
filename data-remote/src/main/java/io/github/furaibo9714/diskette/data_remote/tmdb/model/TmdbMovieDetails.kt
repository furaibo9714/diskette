package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbMovieDetails(
  val id: Long?,
  val title: String?,
  val original_title: String?,
  val overview: String?,
  val release_date: String?,
  val runtime: Int?,
  val genres: List<TmdbGenre>?,
  val production_countries: List<TmdbProductionCountry>?,
  val status: String?,
  val vote_average: Float?,
  val vote_count: Long?,
  val homepage: String?,
  val original_language: String?,
  val videos: TmdbVideosResponse?,
  val release_dates: TmdbReleaseDatesResponse?,
  val belongs_to_collection: TmdbCollectionSummary?,
)

@JsonClass(generateAdapter = true)
data class TmdbCollectionSummary(
  val id: Long?,
  val name: String?,
  val overview: String?,
)

@JsonClass(generateAdapter = true)
data class TmdbCollectionDetails(
  val id: Long?,
  val name: String?,
  val overview: String?,
  val parts: List<TmdbMovieDetails>?,
)

@JsonClass(generateAdapter = true)
data class TmdbProductionCountry(
  val iso_3166_1: String?,
  val name: String?,
)

@JsonClass(generateAdapter = true)
data class TmdbReleaseDatesResponse(
  val results: List<TmdbReleaseDatesCountry>?,
)

@JsonClass(generateAdapter = true)
data class TmdbReleaseDatesCountry(
  val iso_3166_1: String?,
  val release_dates: List<TmdbReleaseDateEntry>?,
)

@JsonClass(generateAdapter = true)
data class TmdbReleaseDateEntry(
  val certification: String?,
)
