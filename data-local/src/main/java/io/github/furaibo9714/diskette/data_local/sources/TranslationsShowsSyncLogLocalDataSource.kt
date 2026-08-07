package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.TranslationsSyncLog

interface TranslationsShowsSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsSyncLog>

  suspend fun getById(idTrakt: Long): TranslationsSyncLog?

  suspend fun upsert(log: TranslationsSyncLog)

  suspend fun deleteAll()
}
