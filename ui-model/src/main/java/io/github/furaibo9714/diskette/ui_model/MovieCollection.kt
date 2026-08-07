package io.github.furaibo9714.diskette.ui_model

data class MovieCollection(
  val id: IdTrakt,
  val name: String,
  val description: String,
  val itemCount: Int,
) {

  companion object {
    val EMPTY = MovieCollection(IdTrakt(-1), "", "", -1)
  }
}
