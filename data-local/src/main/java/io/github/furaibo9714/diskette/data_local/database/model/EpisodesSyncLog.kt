package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_episodes_log")
data class EpisodesSyncLog(
  @PrimaryKey @ColumnInfo(name = "show_media_id", defaultValue = "") val mediaId: String,
  @ColumnInfo(name = "synced_at", defaultValue = "0") val syncedAt: Long,
)
