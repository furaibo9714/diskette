package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MyShow
import io.github.furaibo9714.diskette.data_local.database.model.Show

interface MyShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAll(ids: List<String>): List<Show>

  suspend fun getAllRecent(limit: Int): List<Show>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Show?

  suspend fun updateWatchedAt(
    mediaId: String,
    watchedAt: Long,
  )

  suspend fun insert(shows: List<MyShow>)

  suspend fun deleteById(mediaId: String)

  suspend fun checkExists(mediaId: String): Boolean
}
