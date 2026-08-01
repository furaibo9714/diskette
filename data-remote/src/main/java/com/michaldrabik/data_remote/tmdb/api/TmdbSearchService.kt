package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.model.TmdbSearchMultiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbSearchService {

  @GET("search/multi")
  suspend fun fetchSearchMulti(
    @Query("query") query: String,
    @Query("page") page: Int = 1,
    @Query("include_adult") includeAdult: Boolean = false,
  ): TmdbSearchMultiResponse
}
