package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.data_local.sources.SeasonsLocalDataSource

@Dao
interface SeasonsDao : SeasonsLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(items: List<Season>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun update(items: List<Season>)

  @Delete
  override suspend fun delete(items: List<Season>)

  @Query("SELECT * FROM seasons WHERE media_id IN (:mediaIds)")
  override suspend fun getAll(mediaIds: List<String>): List<Season>

  @Transaction
  override suspend fun getAllByShowsIds(mediaIds: List<String>): List<Season> {
    val result = mutableListOf<Season>()
    val chunks = mediaIds.chunked(50)
    chunks.forEach { chunk ->
      result += getAllByShowsIdsChunk(chunk)
    }
    return result
  }

  @Query("SELECT * FROM seasons WHERE show_media_id IN (:mediaIds)")
  override suspend fun getAllByShowsIdsChunk(mediaIds: List<String>): List<Season>

  @Query("SELECT * FROM seasons WHERE is_watched = 1")
  override suspend fun getAllWatched(): List<Season>

  @Query("SELECT * FROM seasons WHERE show_media_id IN (:mediaIds) AND is_watched = 1")
  override suspend fun getAllWatchedForShows(mediaIds: List<String>): List<Season>

  @Query("SELECT media_id FROM seasons WHERE show_media_id IN (:mediaIds) AND is_watched = 1")
  override suspend fun getAllWatchedIdsForShows(mediaIds: List<String>): List<String>

  @Query("SELECT * FROM seasons WHERE show_media_id = :mediaId")
  override suspend fun getAllByShowId(mediaId: String): List<Season>

  @Query("SELECT * FROM seasons WHERE media_id = :mediaId")
  override suspend fun getById(mediaId: String): Season?

  @Transaction
  override suspend fun upsert(items: List<Season>) {
    val result = insert(items)
    val updateList = mutableListOf<Season>()

    result.forEachIndexed { index, id ->
      if (id == -1L) updateList.add(items[index])
    }

    if (updateList.isNotEmpty()) update(updateList)
  }

  @Query("DELETE FROM seasons WHERE show_media_id = :showMediaId")
  override suspend fun deleteAllForShow(showMediaId: String)
}
