package io.github.furaibo9714.diskette.data_remote.di.module

import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbMoviesService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbSearchService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbShowsService
import io.github.furaibo9714.diskette.data_remote.trakt.TmdbBackedTraktRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

  @Provides
  @Singleton
  fun providesMediaRemoteDataSource(
    @Named("retrofitTmdb") tmdbRetrofit: Retrofit,
  ): TraktRemoteDataSource =
    TmdbBackedTraktRemoteDataSource(
      tmdbSearch = tmdbRetrofit.create(TmdbSearchService::class.java),
      tmdbShows = tmdbRetrofit.create(TmdbShowsService::class.java),
      tmdbMovies = tmdbRetrofit.create(TmdbMoviesService::class.java),
      tmdbPeople = tmdbRetrofit.create(TmdbService::class.java),
    )
}
