package com.michaldrabik.ui_settings.sections.floppy.cases

import com.michaldrabik.repository.floppy.FloppyConnectionManager
import javax.inject.Inject

class SettingsFloppyCase @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
) {

  fun isConfigured() = connectionManager.isConfigured()

  fun getBaseUrl() = connectionManager.getBaseUrl()

  suspend fun testConnection(
    baseUrl: String,
    apiKey: String,
  ) = connectionManager.testConnection(baseUrl, apiKey)

  fun saveConnection(
    baseUrl: String,
    apiKey: String,
  ) = connectionManager.saveConnection(baseUrl, apiKey)

  fun disconnect() = connectionManager.clearConnection()
}
