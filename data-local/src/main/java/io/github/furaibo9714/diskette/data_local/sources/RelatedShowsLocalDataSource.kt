package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.RelatedShow

interface RelatedShowsLocalDataSource {

  suspend fun insert(items: List<RelatedShow>): List<Long>

  suspend fun getAllById(mediaId: String): List<RelatedShow>

  suspend fun getAll(): List<RelatedShow>

  suspend fun deleteById(mediaId: String)
}
