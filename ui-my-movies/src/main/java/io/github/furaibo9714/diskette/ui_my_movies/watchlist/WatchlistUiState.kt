package io.github.furaibo9714.diskette.ui_my_movies.watchlist

import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_my_movies.common.recycler.CollectionListItem

data class WatchlistUiState(
  val items: List<CollectionListItem> = emptyList(),
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
  val resetScroll: Event<Boolean>? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
)
