@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_progress.main

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.EpisodeBundle
import io.github.furaibo9714.diskette.ui_model.ProgressDateSelectionType
import io.github.furaibo9714.diskette.ui_model.Show

data class EpisodeCheckActionUiEvent(
  val episode: EpisodeBundle,
  val dateSelectionType: ProgressDateSelectionType,
) : Event<EpisodeBundle>(episode)

data class OpenEpisodeDetails(
  val show: Show,
  val episode: Episode,
  val isWatched: Boolean,
) : Event<Episode>(episode)

object RequestWidgetsUpdate : Event<Unit>(Unit)
