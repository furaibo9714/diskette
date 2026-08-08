package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.MovieStreaming
import io.github.furaibo9714.diskette.data_local.sources.MovieStreamingsLocalDataSource

@Dao
interface MovieStreamingsDao :
  BaseDao<MovieStreaming>,
  MovieStreamingsLocalDataSource {

  @Transaction
  override suspend fun replace(
    mediaId: String,
    entities: List<MovieStreaming>,
  ) {
    deleteById(mediaId)
    insert(entities)
  }

  @Query("SELECT * FROM movies_streamings WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): List<MovieStreaming>

  @Query("DELETE FROM movies_streamings WHERE media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)

  @Query("DELETE FROM movies_streamings")
  override suspend fun deleteAll()
}
