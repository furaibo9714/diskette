package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow

interface WatchlistShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Show?

  suspend fun insert(show: WatchlistShow)

  suspend fun deleteById(mediaId: String)

  suspend fun checkExists(mediaId: String): Boolean
}
