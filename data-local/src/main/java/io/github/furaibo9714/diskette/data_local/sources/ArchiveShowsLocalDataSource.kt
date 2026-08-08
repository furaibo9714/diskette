package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.database.model.Show

interface ArchiveShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAll(ids: List<String>): List<Show>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Show?

  suspend fun insert(show: ArchiveShow)

  suspend fun deleteById(mediaId: String)
}
