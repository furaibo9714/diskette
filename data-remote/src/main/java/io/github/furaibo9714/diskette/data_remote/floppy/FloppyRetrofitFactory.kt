package io.github.furaibo9714.diskette.data_remote.floppy

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Floppy server URL is user-supplied and can change at runtime, unlike the other services in
 * this module whose Retrofit clients are fixed at build time. This factory builds a client per
 * (baseUrl, apiKey) pair on demand and caches it until the connection changes.
 */
@Singleton
class FloppyRetrofitFactory @Inject constructor(
  private val moshi: Moshi,
) {

  private val cache = mutableMapOf<String, Retrofit>()

  fun create(
    baseUrl: String,
    apiKey: String,
  ): Retrofit {
    val normalizedUrl = normalizeBaseUrl(baseUrl)
    val cacheKey = "$normalizedUrl|$apiKey"
    return cache.getOrPut(cacheKey) {
      val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(TIMEOUT_DURATION)
        .readTimeout(TIMEOUT_DURATION)
        .writeTimeout(TIMEOUT_DURATION)
        .addInterceptor(FloppyHeadersInterceptor(apiKey))
        .build()

      Retrofit
        .Builder()
        .baseUrl(normalizedUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    }
  }

  private fun normalizeBaseUrl(url: String): String {
    val trimmed = url.trim().removeSuffix("/")
    return "$trimmed/"
  }

  companion object {
    private val TIMEOUT_DURATION = Duration.ofSeconds(30)
  }
}
