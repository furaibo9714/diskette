package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MoviesSyncLog

interface MoviesSyncLogLocalDataSource {

  suspend fun getAll(): List<MoviesSyncLog>

  suspend fun upsert(log: MoviesSyncLog)
}
