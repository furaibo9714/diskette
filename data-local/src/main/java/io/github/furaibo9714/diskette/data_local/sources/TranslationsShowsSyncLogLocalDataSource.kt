package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.TranslationsSyncLog

interface TranslationsShowsSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsSyncLog>

  suspend fun getById(mediaId: String): TranslationsSyncLog?

  suspend fun upsert(log: TranslationsSyncLog)

  suspend fun deleteAll()
}
