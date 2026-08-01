package com.michaldrabik.data_remote.floppy

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Stores the single Floppy server connection (URL/API key) in the same SharedPreferences file
 * Trakt's token is stored in. Floppy authenticates with a static API key the user pastes in from
 * their own instance, so - unlike Trakt - there's no OAuth token-refresh to manage here.
 */
@Singleton
class FloppyConnectionProvider @Inject constructor(
  @Named("networkPreferences") private val sharedPreferences: SharedPreferences,
) {

  fun getBaseUrl(): String? = sharedPreferences.getString(KEY_BASE_URL, null)

  fun getApiKey(): String? = sharedPreferences.getString(KEY_API_KEY, null)

  fun isConfigured(): Boolean = !getBaseUrl().isNullOrBlank() && !getApiKey().isNullOrBlank()

  fun saveConnection(
    baseUrl: String,
    apiKey: String,
  ) {
    sharedPreferences.edit(commit = true) {
      putString(KEY_BASE_URL, baseUrl)
      putString(KEY_API_KEY, apiKey)
    }
  }

  fun clear() {
    sharedPreferences.edit(commit = true) {
      remove(KEY_BASE_URL)
      remove(KEY_API_KEY)
    }
  }

  companion object {
    private const val KEY_BASE_URL = "FLOPPY_BASE_URL"
    private const val KEY_API_KEY = "FLOPPY_API_KEY"
  }
}
