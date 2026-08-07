package io.github.furaibo9714.diskette.ui_show.sections.nextepisode.helpers

import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Show
import java.time.format.DateTimeFormatter

data class NextEpisodeBundle(
  val nextEpisode: Pair<Show, Episode>,
  val isWatched: Boolean,
  val dateFormat: DateTimeFormatter? = null,
)
