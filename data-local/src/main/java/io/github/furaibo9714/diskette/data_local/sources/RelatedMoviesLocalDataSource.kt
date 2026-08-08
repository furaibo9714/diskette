package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.RelatedMovie

interface RelatedMoviesLocalDataSource {

  suspend fun insert(items: List<RelatedMovie>): List<Long>

  suspend fun getAllById(mediaId: String): List<RelatedMovie>

  suspend fun getAll(): List<RelatedMovie>

  suspend fun deleteById(mediaId: String)
}
