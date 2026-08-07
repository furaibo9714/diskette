package io.github.furaibo9714.diskette.ui_discover

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_discover.recycler.DiscoverListItem
import io.github.furaibo9714.diskette.ui_model.DiscoverFilters

data class DiscoverUiState(
  val items: List<DiscoverListItem>? = null,
  val isLoading: Boolean? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
  var filters: DiscoverFilters? = null,
  var resetScroll: Event<Boolean>? = null,
)
