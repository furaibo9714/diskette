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
  tableName = "movies_streamings",
  indices = [
    Index(value = ["media_id"]),
    Index(value = ["id_tmdb"]),
  ],
  foreignKeys = [
    ForeignKey(
      entity = Movie::class,
      parentColumns = arrayOf("media_id"),
      childColumns = arrayOf("media_id"),
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
@TypeConverters(DateConverter::class)
data class MovieStreaming(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "id_tmdb") val idTmdb: Long,
  @ColumnInfo(name = "type") val type: String?,
  @ColumnInfo(name = "provider_id") val providerId: Long?,
  @ColumnInfo(name = "provider_name") val providerName: String?,
  @ColumnInfo(name = "display_priority") val displayPriority: Long?,
  @ColumnInfo(name = "logo_path") val logoPath: String?,
  @ColumnInfo(name = "link") val link: String?,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
