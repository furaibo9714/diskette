package io.github.furaibo9714.diskette.ui_discover_movies

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_discover_movies.recycler.DiscoverMovieListItem
import io.github.furaibo9714.diskette.ui_model.DiscoverFilters

data class DiscoverMoviesUiState(
  val items: List<DiscoverMovieListItem>? = null,
  val isLoading: Boolean? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: Event<Boolean>? = null,
)
