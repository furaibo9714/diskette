@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_my_movies.filters

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class CollectionFiltersUiEvent<T>(
  action: T,
) : Event<T>(action) {
  object ApplyFilters : CollectionFiltersUiEvent<Unit>(Unit)

  object CloseFilters : CollectionFiltersUiEvent<Unit>(Unit)
}
