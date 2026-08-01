package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.FloppySyncQueue
import com.michaldrabik.data_local.sources.FloppySyncQueueLocalDataSource

@Dao
interface FloppySyncQueueDao : FloppySyncQueueLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  override suspend fun insert(items: List<FloppySyncQueue>): List<Long>

  @Delete
  override suspend fun delete(items: List<FloppySyncQueue>)

  @Query("SELECT * FROM floppy_sync_queue ORDER BY created_at ASC")
  override suspend fun getAll(): List<FloppySyncQueue>

  @Query("DELETE FROM floppy_sync_queue")
  override suspend fun deleteAll()
}
