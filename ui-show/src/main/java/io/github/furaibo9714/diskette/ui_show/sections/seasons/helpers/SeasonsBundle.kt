package io.github.furaibo9714.diskette.ui_show.sections.seasons.helpers

import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem

data class SeasonsBundle(
  val seasons: List<SeasonListItem>?,
  val isLocal: Boolean,
)
