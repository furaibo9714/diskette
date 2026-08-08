package io.github.furaibo9714.diskette.ui_model

data class MovieCollection(
  val id: MediaId,
  val name: String,
  val description: String,
  val itemCount: Int,
) {

  companion object {
    val EMPTY = MovieCollection(MediaId.EMPTY, "", "", -1)
  }
}
