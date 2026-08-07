package io.github.furaibo9714.diskette.ui_model

import androidx.annotation.StringRes

enum class MyMoviesSection(
  @StringRes val displayString: Int,
) {
  RECENTS(
    displayString = R.string.textHeaderRecentlyAdded,
  ),
  ALL(
    displayString = R.string.textHeaderAll,
  ),
}
