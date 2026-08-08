package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_discover",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class DiscoverShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id", defaultValue = "", index = true) val mediaId: String,
  @ColumnInfo(name = "created_at", defaultValue = "-1") val createdAt: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long,
)
