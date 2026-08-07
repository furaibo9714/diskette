package io.github.furaibo9714.diskette.data_remote.tmdb.api

import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbFindResponse
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSearchMultiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbSearchService {

  @GET("search/multi")
  suspend fun fetchSearchMulti(
    @Query("query") query: String,
    @Query("page") page: Int = 1,
    @Query("include_adult") includeAdult: Boolean = false,
  ): TmdbSearchMultiResponse

  @GET("find/{externalId}")
  suspend fun findByExternalId(
    @Path("externalId") externalId: String,
    @Query("external_source") externalSource: String,
  ): TmdbFindResponse
}
