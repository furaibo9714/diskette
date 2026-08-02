package com.michaldrabik.ui_discover

import com.michaldrabik.ui_base.floppy.FloppySyncProgressState
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters

data class DiscoverUiState(
  val items: List<DiscoverListItem>? = null,
  val isLoading: Boolean? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: Event<Boolean>? = null,
)
