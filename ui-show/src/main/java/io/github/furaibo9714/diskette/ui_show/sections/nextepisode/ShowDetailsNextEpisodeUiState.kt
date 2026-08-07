package io.github.furaibo9714.diskette.ui_show.sections.nextepisode

import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_show.sections.nextepisode.helpers.NextEpisodeBundle

data class ShowDetailsNextEpisodeUiState(
  val nextEpisode: NextEpisodeBundle? = null,
  val spoilersSettings: SpoilersSettings? = null,
)
