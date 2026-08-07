package io.github.furaibo9714.diskette.ui_show.sections.streamings

import io.github.furaibo9714.diskette.ui_model.StreamingService

data class ShowDetailsStreamingsUiState(
  val streamings: StreamingsState? = null,
) {

  data class StreamingsState(
    val streamings: List<StreamingService>,
    val isLocal: Boolean,
  )
}
