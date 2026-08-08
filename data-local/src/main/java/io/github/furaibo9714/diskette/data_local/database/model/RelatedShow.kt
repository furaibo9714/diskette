package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
  tableName = "shows_related",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("related_show_media_id"),
      onDelete = CASCADE,
    ),
  ],
)
data class RelatedShow(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id", defaultValue = "") val mediaId: String,
  @ColumnInfo(name = "related_show_media_id", defaultValue = "", index = true) val relatedShowMediaId: String,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long,
) {

  companion object {
    fun fromMediaId(
      mediaId: String,
      relatedShowMediaId: String,
      nowUtcMillis: Long,
    ): RelatedShow =
      RelatedShow(
        mediaId = mediaId,
        relatedShowMediaId = relatedShowMediaId,
        updatedAt = nowUtcMillis,
      )
  }
}
