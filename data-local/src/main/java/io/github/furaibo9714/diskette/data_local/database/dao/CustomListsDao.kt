package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.furaibo9714.diskette.data_local.database.model.CustomList
import io.github.furaibo9714.diskette.data_local.sources.CustomListsLocalDataSource

@Dao
interface CustomListsDao : CustomListsLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  override suspend fun insert(items: List<CustomList>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun update(items: List<CustomList>)

  @Query("SELECT * FROM custom_lists ORDER BY created_at DESC")
  override suspend fun getAll(): List<CustomList>

  @Query("SELECT * FROM custom_lists WHERE id == :id")
  override suspend fun getById(id: Long): CustomList?

  @Query("UPDATE custom_lists SET media_id = :mediaId, id_slug = :idSlug, updated_at = :timestamp WHERE id == :id")
  override suspend fun updateMediaId(
    id: Long,
    mediaId: String,
    idSlug: String,
    timestamp: Long,
  )

  @Query("UPDATE custom_lists SET updated_at = :timestamp WHERE id == :id")
  override suspend fun updateTimestamp(
    id: Long,
    timestamp: Long,
  )

  @Query(
    "UPDATE custom_lists SET sort_by_local = :sortBy, sort_how_local = :sortHow, updated_at = :timestamp " +
      "WHERE id == :id",
  )
  override suspend fun updateSortByLocal(
    id: Long,
    sortBy: String,
    sortHow: String,
    timestamp: Long,
  )

  @Query("UPDATE custom_lists SET filter_type_local = :filterType, updated_at = :timestamp WHERE id == :id")
  override suspend fun updateFilterTypeLocal(
    id: Long,
    filterType: String,
    timestamp: Long,
  )

  @Query("DELETE FROM custom_lists WHERE id == :id")
  override suspend fun deleteById(id: Long)

  @Query("DELETE FROM custom_lists")
  override suspend fun deleteAll()
}
