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
  tableName = "movies_collections_items",
  indices = [
    Index(value = ["media_id"]),
    Index(value = ["collection_media_id"]),
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
data class MovieCollectionItem(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "collection_media_id") val collectionMediaId: String,
  @ColumnInfo(name = "rank") val rank: Int,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
)
