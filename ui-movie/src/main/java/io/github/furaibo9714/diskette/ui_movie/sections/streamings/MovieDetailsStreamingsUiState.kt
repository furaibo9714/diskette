package io.github.furaibo9714.diskette.ui_movie.sections.streamings

import io.github.furaibo9714.diskette.ui_model.StreamingService

data class MovieDetailsStreamingsUiState(
  val streamings: StreamingsState? = null,
) {

  data class StreamingsState(
    val streamings: List<StreamingService>,
    val isLocal: Boolean,
  )
}
