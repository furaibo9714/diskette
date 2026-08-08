package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_my_shows",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class MyShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id", defaultValue = "", index = true) val mediaId: String,
  @ColumnInfo(name = "created_at", defaultValue = "-1") val createdAt: Long,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long,
  @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long?,
) {

  companion object {
    fun fromMediaId(
      mediaId: String,
      createdAt: Long,
      updatedAt: Long,
      watchedAt: Long,
    ) = MyShow(
      mediaId = mediaId,
      createdAt = createdAt,
      updatedAt = updatedAt,
      lastWatchedAt = watchedAt,
    )
  }
}
