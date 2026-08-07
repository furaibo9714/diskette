@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_discover.filters.feed

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class DiscoverFiltersFeedUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : DiscoverFiltersFeedUiEvent<Unit>(Unit)

  object CloseFilters : DiscoverFiltersFeedUiEvent<Unit>(Unit)
}
