package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.TranslationsMoviesSyncLog

interface TranslationsMoviesSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsMoviesSyncLog>

  suspend fun getById(mediaId: String): TranslationsMoviesSyncLog?

  suspend fun upsert(log: TranslationsMoviesSyncLog)

  suspend fun deleteAll()
}
