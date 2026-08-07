package io.github.furaibo9714.diskette.ui_model

import androidx.annotation.StringRes
import io.github.furaibo9714.diskette.ui_model.ShowStatus.CANCELED
import io.github.furaibo9714.diskette.ui_model.ShowStatus.ENDED
import io.github.furaibo9714.diskette.ui_model.ShowStatus.IN_PRODUCTION
import io.github.furaibo9714.diskette.ui_model.ShowStatus.PLANNED
import io.github.furaibo9714.diskette.ui_model.ShowStatus.RETURNING

enum class MyShowsSection(
  @StringRes val displayString: Int,
  val allowedStatuses: List<ShowStatus> = emptyList(),
) {
  RECENTS(
    displayString = R.string.textHeaderRecentlyAdded,
  ),
  WATCHING(
    allowedStatuses = listOf(RETURNING),
    displayString = R.string.textHeaderWatching,
  ),
  FINISHED(
    allowedStatuses = listOf(CANCELED, ENDED),
    displayString = R.string.textHeaderFinished,
  ),
  UPCOMING(
    allowedStatuses = listOf(IN_PRODUCTION, PLANNED, ShowStatus.UPCOMING),
    displayString = R.string.textHeaderReturning,
  ),
  ALL(
    displayString = R.string.textHeaderAll,
  ),
}
