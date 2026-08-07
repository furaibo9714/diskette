package io.github.furaibo9714.diskette.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbVideosResponse(
  val results: List<TmdbVideo>?,
)

@JsonClass(generateAdapter = true)
data class TmdbVideo(
  val key: String?,
  val site: String?,
  val type: String?,
  val official: Boolean?,
)
