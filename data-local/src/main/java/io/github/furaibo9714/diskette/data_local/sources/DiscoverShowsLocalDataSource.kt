package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.DiscoverShow

interface DiscoverShowsLocalDataSource {

  suspend fun getAll(): List<DiscoverShow>

  suspend fun getMostRecent(): DiscoverShow?

  suspend fun upsert(shows: List<DiscoverShow>)

  suspend fun deleteAll()

  suspend fun replace(shows: List<DiscoverShow>)
}
