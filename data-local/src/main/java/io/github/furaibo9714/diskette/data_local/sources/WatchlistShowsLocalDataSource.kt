package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow

interface WatchlistShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Show?

  suspend fun insert(show: WatchlistShow)

  suspend fun deleteById(traktId: Long)

  suspend fun checkExists(traktId: Long): Boolean
}
