package io.github.furaibo9714.diskette.data_remote.floppy

import okhttp3.Interceptor
import okhttp3.Response

class FloppyHeadersInterceptor(
  private val apiKey: String,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain
      .request()
      .newBuilder()
      .addHeader("X-API-Key", apiKey)
      .addHeader("Content-Type", "application/json")
      .build()
    return chain.proceed(request)
  }
}
