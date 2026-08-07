package io.github.furaibo9714.diskette.data_remote.di.module

import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbMoviesService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbSearchService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbShowsService
import io.github.furaibo9714.diskette.data_remote.trakt.TmdbBackedTraktRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.api.TraktApi
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktMoviesService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktPeopleService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktSearchService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktShowsService
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
