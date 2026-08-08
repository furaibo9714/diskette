package io.github.furaibo9714.diskette.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class ShowSearch(
  @PrimaryKey @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "title") val title: String,
)
