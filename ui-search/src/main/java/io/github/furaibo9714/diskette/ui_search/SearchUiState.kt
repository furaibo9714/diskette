package io.github.furaibo9714.diskette.ui_search

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.RecentSearch
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import io.github.furaibo9714.diskette.ui_search.utilities.SearchOptions

data class SearchUiState(
  val searchItems: List<SearchListItem>? = null,
  val searchItemsAnimate: Event<Boolean>? = null,
  val recentSearchItems: List<RecentSearch>? = null,
  val suggestionsItems: List<SearchListItem>? = null,
  val searchOptions: SearchOptions? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
  val isSearching: Boolean = false,
  val isEmpty: Boolean = false,
  val isInitial: Boolean = false,
  val isFiltersVisible: Boolean = false,
  val isMoviesEnabled: Boolean = false,
  val resetScroll: Event<Boolean>? = null,
)
