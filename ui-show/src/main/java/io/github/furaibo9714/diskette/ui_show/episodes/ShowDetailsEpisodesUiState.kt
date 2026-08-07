package io.github.furaibo9714.diskette.ui_show.episodes

import io.github.furaibo9714.diskette.ui_show.episodes.recycler.EpisodeListItem
import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem

data class ShowDetailsEpisodesUiState(
  val season: SeasonListItem? = null,
  val episodes: List<EpisodeListItem>? = null,
  val isInitialLoad: Boolean? = null,
)
