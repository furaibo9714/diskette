package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.ShowStreaming
import io.github.furaibo9714.diskette.data_local.sources.ShowStreamingsLocalDataSource

@Dao
interface ShowStreamingsDao :
  BaseDao<ShowStreaming>,
  ShowStreamingsLocalDataSource {

  @Transaction
  override suspend fun replace(
    mediaId: String,
    entities: List<ShowStreaming>,
  ) {
    deleteById(mediaId)
    insert(entities)
  }

  @Query("SELECT * FROM shows_streamings WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): List<ShowStreaming>

  @Query("DELETE FROM shows_streamings WHERE media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)

  @Query("DELETE FROM shows_streamings")
  override suspend fun deleteAll()
}
