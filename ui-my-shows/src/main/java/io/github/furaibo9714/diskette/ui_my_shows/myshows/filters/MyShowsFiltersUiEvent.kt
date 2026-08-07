@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_my_shows.myshows.filters

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class MyShowsFiltersUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : MyShowsFiltersUiEvent<Unit>(Unit)

  object CloseFilters : MyShowsFiltersUiEvent<Unit>(Unit)
}
