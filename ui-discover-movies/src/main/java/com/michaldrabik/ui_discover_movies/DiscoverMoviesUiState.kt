package com.michaldrabik.ui_discover_movies

import com.michaldrabik.ui_base.floppy.FloppySyncProgressState
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverMoviesUiState(
  val items: List<DiscoverMovieListItem>? = null,
  val isLoading: Boolean? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: Event<Boolean>? = null,
)
