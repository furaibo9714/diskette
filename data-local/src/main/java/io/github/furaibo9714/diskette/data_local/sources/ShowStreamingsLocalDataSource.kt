package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ShowStreaming

interface ShowStreamingsLocalDataSource {

  suspend fun replace(
    mediaId: String,
    entities: List<ShowStreaming>,
  )

  suspend fun getById(mediaId: String): List<ShowStreaming>

  suspend fun deleteById(mediaId: String)

  suspend fun deleteAll()
}
