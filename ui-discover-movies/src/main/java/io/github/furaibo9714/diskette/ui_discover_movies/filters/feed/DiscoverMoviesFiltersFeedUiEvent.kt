@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_discover_movies.filters.feed

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class DiscoverMoviesFiltersFeedUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : DiscoverMoviesFiltersFeedUiEvent<Unit>(Unit)

  object CloseFilters : DiscoverMoviesFiltersFeedUiEvent<Unit>(Unit)
}
