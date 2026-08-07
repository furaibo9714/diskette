package io.github.furaibo9714.diskette.data_remote.tmdb.api

import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbMovieDetails
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSearchMultiResponse
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbTranslationsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbMoviesService {

  @GET("movie/{tmdbId}")
  suspend fun fetchMovieDetails(
    @Path("tmdbId") tmdbId: Long,
    @Query("append_to_response") appendToResponse: String = "videos,release_dates",
  ): TmdbMovieDetails

  @GET("movie/{tmdbId}/translations")
  suspend fun fetchMovieTranslations(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbTranslationsResponse

  @GET("movie/{tmdbId}/similar")
  suspend fun fetchSimilarMovies(
    @Path("tmdbId") tmdbId: Long,
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("trending/movie/{timeWindow}")
  suspend fun fetchTrendingMovies(
    @Path("timeWindow") timeWindow: String = "week",
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("movie/popular")
  suspend fun fetchPopularMovies(
    @Query("page") page: Int = 1,
  ): TmdbSearchMultiResponse

  @GET("discover/movie")
  suspend fun discoverMovies(
    @Query("page") page: Int = 1,
    @Query("with_genres") withGenres: String? = null,
    @Query("sort_by") sortBy: String = "popularity.desc",
    @Query("primary_release_date.gte") releaseDateFrom: String? = null,
  ): TmdbSearchMultiResponse
}
