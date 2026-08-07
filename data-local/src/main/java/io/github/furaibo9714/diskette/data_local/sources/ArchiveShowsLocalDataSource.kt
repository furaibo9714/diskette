package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.database.model.Show

interface ArchiveShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAll(ids: List<Long>): List<Show>

  suspend fun getAllTraktIds(): List<Long>

  suspend fun getById(traktId: Long): Show?

  suspend fun insert(show: ArchiveShow)

  suspend fun deleteById(traktId: Long)
}
