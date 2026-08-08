package io.github.furaibo9714.diskette.ui_model

data class SearchResult(
  val order: Int,
  val show: Show,
  val movie: Movie,
) {

  val isShow = show != Show.EMPTY

  val mediaId = if (show != Show.EMPTY) show.mediaId else movie.mediaId
}
