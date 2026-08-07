package io.github.furaibo9714.diskette.data_remote.trakt.model

data class TraktUser(
  val username: String,
  val images: Image?,
) {

  data class Image(
    val avatar: ImageDetails?,
  )

  data class ImageDetails(
    val full: String?,
  )
}
