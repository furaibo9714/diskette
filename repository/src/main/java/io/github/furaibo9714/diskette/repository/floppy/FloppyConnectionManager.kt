package io.github.furaibo9714.diskette.repository.floppy

import io.github.furaibo9714.diskette.data_remote.floppy.FloppyConnectionProvider
import io.github.furaibo9714.diskette.data_remote.floppy.FloppyRetrofitFactory
import io.github.furaibo9714.diskette.data_remote.floppy.api.FloppyService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Connection management (server URL/API key) for the user's Floppy instance - Floppy authenticates
 * with a static API key the user pastes in from their own instance's account settings, no OAuth.
 */
@Singleton
class FloppyConnectionManager @Inject constructor(
  private val connectionProvider: FloppyConnectionProvider,
  private val retrofitFactory: FloppyRetrofitFactory,
) {

  fun isConfigured(): Boolean = connectionProvider.isConfigured()

  fun getBaseUrl(): String? = connectionProvider.getBaseUrl()

  fun getApiKey(): String? = connectionProvider.getApiKey()

  suspend fun testConnection(
    baseUrl: String,
    apiKey: String,
  ): Result<Unit> =
    runCatching {
      retrofit(baseUrl, apiKey).create(FloppyService::class.java).info()
      Unit
    }

  fun saveConnection(
    baseUrl: String,
    apiKey: String,
  ) {
    connectionProvider.saveConnection(baseUrl, apiKey)
  }

  fun clearConnection() {
    connectionProvider.clear()
  }

  fun service(): FloppyService {
    val baseUrl = connectionProvider.getBaseUrl()
    val apiKey = connectionProvider.getApiKey()
    check(!baseUrl.isNullOrBlank() && !apiKey.isNullOrBlank()) { "Floppy connection is not configured." }
    return retrofit(baseUrl, apiKey).create(FloppyService::class.java)
  }

  private fun retrofit(
    baseUrl: String,
    apiKey: String,
  ) = retrofitFactory.create(baseUrl, apiKey)
}
