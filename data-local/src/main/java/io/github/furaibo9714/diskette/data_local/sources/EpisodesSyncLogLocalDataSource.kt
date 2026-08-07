package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.EpisodesSyncLog

interface EpisodesSyncLogLocalDataSource {

  suspend fun getAll(): List<EpisodesSyncLog>

  suspend fun upsert(log: EpisodesSyncLog)
}
