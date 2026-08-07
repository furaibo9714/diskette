@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_discover.filters.genres

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class DiscoverFiltersGenresUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : DiscoverFiltersGenresUiEvent<Unit>(Unit)

  object CloseFilters : DiscoverFiltersGenresUiEvent<Unit>(Unit)
}
