package io.github.furaibo9714.diskette.ui_model

enum class ImageType(
  val id: Int,
  val key: String,
) {
  POSTER(1, "poster"),
  FANART(2, "fanart"),
  FANART_WIDE(3, "fanart"),
  PROFILE(6, "profile"),
  ;

  fun getSpan(): Int =
    when (this) {
      POSTER -> 1
      FANART -> 2
      FANART_WIDE -> 3
      PROFILE -> 1
    }
}
