package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import io.github.furaibo9714.diskette.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "ratings",
  primaryKeys = ["media_id", "type"],
  indices = [
    Index(value = ["media_id", "type"], unique = false),
  ],
)
@TypeConverters(DateConverter::class)
data class Rating(
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "rating") val rating: Int,
  @ColumnInfo(name = "season_number") val seasonNumber: Int?,
  @ColumnInfo(name = "episode_number") val episodeNumber: Int?,
  @ColumnInfo(name = "rated_at") val ratedAt: ZonedDateTime,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
