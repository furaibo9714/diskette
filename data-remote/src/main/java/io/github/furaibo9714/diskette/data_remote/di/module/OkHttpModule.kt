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

  /**
   * Flip to [HttpLoggingInterceptor.Level.BODY] for a session spent debugging a payload, then flip
   * back. It is not the default because response bodies bury everything else: during a full Floppy
   * import TMDB payloads filled logcat fast enough to roll the buffer and lose the sync's own
   * lines. [HttpLoggingInterceptor.Level.BASIC] keeps method, URL, status, size and duration.
   */
  private val DEBUG_LOG_LEVEL = HttpLoggingInterceptor.Level.BASIC

  @Provides
  @Singleton
  fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      /**
       * Header logging would otherwise print the credential on every request. Floppy's client
       * builds its own OkHttpClient without this interceptor today, but its header is redacted too
       * so that wiring it up later cannot quietly start leaking the key.
       */
      redactHeader("Authorization")
      redactHeader("X-API-Key")

      level = when {
        BuildConfig.DEBUG -> DEBUG_LOG_LEVEL
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
