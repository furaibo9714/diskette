package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
  tableName = "movies_related",
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("related_movie_media_id"),
      onDelete = CASCADE,
    ),
  ],
)
data class RelatedMovie(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id", defaultValue = "") val mediaId: String,
  @ColumnInfo(name = "related_movie_media_id", defaultValue = "", index = true) val relatedMovieMediaId: String,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long,
) {

  companion object {
    fun fromMediaId(
      mediaId: String,
      relatedMediaId: String,
      nowUtcMillis: Long,
    ) = RelatedMovie(
      mediaId = mediaId,
      relatedMovieMediaId = relatedMediaId,
      updatedAt = nowUtcMillis,
    )
  }
}
