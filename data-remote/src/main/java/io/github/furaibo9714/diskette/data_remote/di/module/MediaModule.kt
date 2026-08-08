package io.github.furaibo9714.diskette.data_remote.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.media.TmdbMediaRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbMoviesService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbSearchService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbShowsService
import javax.inject.Named
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

  @Provides
  @Singleton
  fun providesMediaRemoteDataSource(
    @Named("retrofitTmdb") tmdbRetrofit: Retrofit,
  ): MediaRemoteDataSource =
    TmdbMediaRemoteDataSource(
      tmdbSearch = tmdbRetrofit.create(TmdbSearchService::class.java),
      tmdbShows = tmdbRetrofit.create(TmdbShowsService::class.java),
      tmdbMovies = tmdbRetrofit.create(TmdbMoviesService::class.java),
      tmdbPeople = tmdbRetrofit.create(TmdbService::class.java),
    )
}
