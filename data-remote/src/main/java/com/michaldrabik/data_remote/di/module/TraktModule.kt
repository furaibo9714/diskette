package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.tmdb.api.TmdbMoviesService
import com.michaldrabik.data_remote.tmdb.api.TmdbSearchService
import com.michaldrabik.data_remote.tmdb.api.TmdbService
import com.michaldrabik.data_remote.tmdb.api.TmdbShowsService
import com.michaldrabik.data_remote.trakt.TmdbBackedTraktRemoteDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.data_remote.trakt.api.service.TraktMoviesService
import com.michaldrabik.data_remote.trakt.api.service.TraktPeopleService
import com.michaldrabik.data_remote.trakt.api.service.TraktSearchService
import com.michaldrabik.data_remote.trakt.api.service.TraktShowsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TraktModule {

  @Provides
  @Singleton
  fun providesTraktApi(
    @Named("retrofitTrakt") retrofit: Retrofit,
    @Named("retrofitTmdb") tmdbRetrofit: Retrofit,
  ): TraktRemoteDataSource {
    val traktApi = TraktApi(
      showsService = retrofit.create(TraktShowsService::class.java),
      moviesService = retrofit.create(TraktMoviesService::class.java),
      searchService = retrofit.create(TraktSearchService::class.java),
      peopleService = retrofit.create(TraktPeopleService::class.java),
    )
    return TmdbBackedTraktRemoteDataSource(
      legacyTrakt = traktApi,
      tmdbSearch = tmdbRetrofit.create(TmdbSearchService::class.java),
      tmdbShows = tmdbRetrofit.create(TmdbShowsService::class.java),
      tmdbMovies = tmdbRetrofit.create(TmdbMoviesService::class.java),
      tmdbPeople = tmdbRetrofit.create(TmdbService::class.java),
    )
  }
}
