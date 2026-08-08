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
  tableName = "people_credits",
  foreignKeys = [
    ForeignKey(
      entity = Show::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("show_media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("movie_media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [
    Index(value = ["person_media_id"]),
    Index(value = ["show_media_id"]),
    Index(value = ["movie_media_id"]),
  ],
)
@TypeConverters(DateConverter::class)
data class PersonCredits(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
  @ColumnInfo(name = "person_media_id") val personMediaId: String,
  @ColumnInfo(name = "show_media_id") val showMediaId: String?,
  @ColumnInfo(name = "movie_media_id") val movieMediaId: String?,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
