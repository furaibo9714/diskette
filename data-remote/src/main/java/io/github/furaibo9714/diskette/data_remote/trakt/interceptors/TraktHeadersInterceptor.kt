package io.github.furaibo9714.diskette.data_remote.trakt.interceptors

import android.os.Build
import io.github.furaibo9714.diskette.data_remote.BuildConfig
import io.github.furaibo9714.diskette.data_remote.Config
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktHeadersInterceptor @Inject constructor() : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val userAgent = Config.traktUserAgent(
      buildVersion = BuildConfig.VER_NAME,
      buildCode = BuildConfig.VER_CODE,
      androidVersion = Build.VERSION.SDK_INT,
    )

    val request = chain
      .request()
      .newBuilder()
      .header("Content-Type", "application/json")
      .header("User-Agent", userAgent)
      .header("trakt-api-key", Config.TRAKT_CLIENT_ID)
      .header("trakt-api-version", Config.TRAKT_VERSION)
      .build()

    return chain.proceed(request)
  }
}
