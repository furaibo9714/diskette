package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbTranslationsResponse(
  val translations: List<TmdbTranslationEntry>?,
)

@JsonClass(generateAdapter = true)
data class TmdbTranslationEntry(
  val iso_3166_1: String?,
  val iso_639_1: String?,
  val data: TmdbTranslationEntryData?,
)

@JsonClass(generateAdapter = true)
data class TmdbTranslationEntryData(
  val title: String?,
  val name: String?,
  val overview: String?,
)
