package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieStreaming

interface MovieStreamingsLocalDataSource {

  suspend fun replace(
    mediaId: String,
    entities: List<MovieStreaming>,
  )

  suspend fun getById(mediaId: String): List<MovieStreaming>

  suspend fun deleteById(mediaId: String)

  suspend fun deleteAll()
}
