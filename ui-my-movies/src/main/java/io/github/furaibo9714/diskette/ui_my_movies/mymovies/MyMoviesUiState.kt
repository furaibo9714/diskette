package io.github.furaibo9714.diskette.ui_my_movies.mymovies

import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_my_movies.mymovies.recycler.MyMoviesItem

data class MyMoviesUiState(
  val items: List<MyMoviesItem>? = null,
  val showEmptyView: Boolean = false,
  val viewMode: ListViewMode = ListViewMode.LIST_NORMAL,
  val resetScroll: Event<Boolean>? = null,
)
