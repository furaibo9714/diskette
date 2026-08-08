package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.github.furaibo9714.diskette.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "movies_collections",
  indices = [
    Index(value = ["media_id"]),
    Index(value = ["movie_media_id"]),
  ],
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("movie_media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
@TypeConverters(DateConverter::class)
data class MovieCollection(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "movie_media_id") val movieMediaId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "description") val description: String,
  @ColumnInfo(name = "item_count") val itemCount: Int,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
