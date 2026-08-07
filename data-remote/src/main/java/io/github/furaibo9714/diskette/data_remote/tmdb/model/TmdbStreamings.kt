package io.github.furaibo9714.diskette.data_remote.tmdb.model

data class TmdbStreamings(
  val id: Long,
  val results: Map<String, TmdbStreamingCountry>,
)
