package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_translations_log")
data class TranslationsSyncLog(
  @PrimaryKey @ColumnInfo(name = "show_media_id") val mediaId: String,
  @ColumnInfo(name = "synced_at") val syncedAt: Long,
)
