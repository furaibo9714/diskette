package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_movies_log")
data class MoviesSyncLog(
  @PrimaryKey @ColumnInfo(name = "movie_media_id", defaultValue = "") val mediaId: String,
  @ColumnInfo(name = "synced_at", defaultValue = "0") val syncedAt: Long,
)
