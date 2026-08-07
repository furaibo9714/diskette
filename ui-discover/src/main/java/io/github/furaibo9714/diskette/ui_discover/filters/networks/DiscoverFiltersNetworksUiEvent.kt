@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_discover.filters.networks

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class DiscoverFiltersNetworksUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : DiscoverFiltersNetworksUiEvent<Unit>(Unit)

  object CloseFilters : DiscoverFiltersNetworksUiEvent<Unit>(Unit)
}
