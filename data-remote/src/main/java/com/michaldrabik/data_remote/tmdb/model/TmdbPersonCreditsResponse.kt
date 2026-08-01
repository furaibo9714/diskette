package com.michaldrabik.data_remote.tmdb.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbPersonCreditsResponse(
  val cast: List<TmdbSearchResultItem>?,
  val crew: List<TmdbSearchResultItem>?,
)
