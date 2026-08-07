package io.github.furaibo9714.diskette.data_remote.di.module

import io.github.furaibo9714.diskette.data_remote.BuildConfig
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbInterceptor
import io.github.furaibo9714.diskette.data_remote.tmdb.interceptors.TmdbRetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Duration
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

  private val TIMEOUT_DURATION = Duration.ofSeconds(60)

  @Provides
  @Singleton
  @Named("okHttpBase")
  fun providesBaseOkHttp(): OkHttpClient = createBaseOkHttpClient().build()

  @Provides
  @Singleton
  @Named("okHttpTmdb")
  fun providesTmdbOkHttp(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    tmdbInterceptor: TmdbInterceptor,
    tmdbRetryInterceptor: TmdbRetryInterceptor,
  ) = createBaseOkHttpClient()
    .addInterceptor(tmdbInterceptor)
    .addInterceptor(tmdbRetryInterceptor)
    .addInterceptor(httpLoggingInterceptor)
    .build()

  @Provides
  @Singleton
  fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      level = when {
        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
      }
    }

  private fun createBaseOkHttpClient() =
    OkHttpClient
      .Builder()
      .writeTimeout(TIMEOUT_DURATION)
      .readTimeout(TIMEOUT_DURATION)
      .callTimeout(TIMEOUT_DURATION)
}
