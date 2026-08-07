package io.github.furaibo9714.diskette.ui_show.quicksetup

import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Season

data class QuickSetupListItem(
  val episode: Episode,
  val season: Season,
  val isHeader: Boolean = false,
  val isChecked: Boolean = false,
)
