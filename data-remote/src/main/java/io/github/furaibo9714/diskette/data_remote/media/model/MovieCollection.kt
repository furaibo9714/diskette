package io.github.furaibo9714.diskette.data_remote.media.model

data class MovieCollection(
  val ids: Ids,
  val name: String,
  val description: String,
  val privacy: String,
  val item_count: Int,
  val likes: Int,
)
