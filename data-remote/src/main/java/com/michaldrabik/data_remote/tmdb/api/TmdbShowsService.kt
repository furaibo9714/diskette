package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.model.TmdbSearchMultiResponse
import com.michaldrabik.data_remote.tmdb.model.TmdbSeasonDetails
import com.michaldrabik.data_remote.tmdb.model.TmdbShowDetails
import com.michaldrabik.data_remote.tmdb.model.TmdbTranslationsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbShowsService {

  @GET("tv/{tmdbId}")
  suspend fun fetchShowDetails(
    @Path("tmdbId") tmdbId: Long,
    @Query("append_to_response") appendToResponse: String = "videos,content_ratings",
  ): TmdbShowDetails

  @GET("tv/{tmdbId}/translations")
  suspend fun fetchShowTranslations(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbTranslationsResponse

  @GET("tv/{tmdbId}/season/{seasonNumber}")
  suspend fun fetchSeasonDetails(
    @Path("tmdbId") tmdbId: Long,
    @Path("seasonNumber") seasonNumber: Int,
  ): TmdbSeasonDetails

  @GET("tv/{tmdbId}/similar")
  suspend fun fetchSimilarShows(
    @Path("tmdbId") tmdbId: Long,
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("trending/tv/{timeWindow}")
  suspend fun fetchTrendingShows(
    @Path("timeWindow") timeWindow: String = "week",
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("tv/popular")
  suspend fun fetchPopularShows(
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("discover/tv")
  suspend fun discoverShows(
    @Query("page") page: Int = 1,
    @Query("with_genres") withGenres: String? = null,
    @Query("sort_by") sortBy: String = "popularity.desc",
    @Query("first_air_date.gte") firstAirDateFrom: String? = null,
  ): TmdbSearchMultiResponse
}
