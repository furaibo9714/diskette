package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Settings

interface SettingsLocalDataSource {

  suspend fun getAll(): Settings

  suspend fun getCount(): Int

  suspend fun upsert(settings: Settings)
}
