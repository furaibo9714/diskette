package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "movies_archive",
  indices = [Index(value = ["media_id"], unique = true)],
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class ArchiveMovie(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
) {

  companion object {
    fun fromMediaId(
      mediaId: String,
      createdAt: Long,
    ) = ArchiveMovie(
      mediaId = mediaId,
      createdAt = createdAt,
      updatedAt = createdAt,
    )
  }
}
