package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "episodes_translations",
  indices = [
    Index(value = ["media_id"], unique = true),
    Index(value = ["show_media_id"]),
  ],
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("show_media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class EpisodeTranslation(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "show_media_id") val showMediaId: String,
  @ColumnInfo(name = "title") val title: String,
  @ColumnInfo(name = "language") val language: String,
  @ColumnInfo(name = "overview") val overview: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
) {

  companion object {
    fun fromMediaId(
      episodeMediaId: String,
      showMediaId: String,
      title: String,
      language: String,
      overview: String,
      createdAt: Long,
    ) = EpisodeTranslation(
      mediaId = episodeMediaId,
      showMediaId = showMediaId,
      title = title,
      language = language,
      overview = overview,
      createdAt = createdAt,
      updatedAt = createdAt,
    )
  }
}
