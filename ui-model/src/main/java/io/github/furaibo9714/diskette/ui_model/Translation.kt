package io.github.furaibo9714.diskette.ui_model

data class Translation(
  val title: String,
  val overview: String,
  val language: String,
) {

  companion object {
    val EMPTY = Translation("", "", "")
  }

  val hasTitle = title.isNotBlank()
}
