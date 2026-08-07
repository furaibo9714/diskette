package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.TranslationsMoviesSyncLog

interface TranslationsMoviesSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsMoviesSyncLog>

  suspend fun getById(idTrakt: Long): TranslationsMoviesSyncLog?

  suspend fun upsert(log: TranslationsMoviesSyncLog)

  suspend fun deleteAll()
}
