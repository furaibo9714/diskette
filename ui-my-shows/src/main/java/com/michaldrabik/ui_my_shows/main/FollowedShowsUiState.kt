package com.michaldrabik.ui_my_shows.main

import com.michaldrabik.ui_base.floppy.FloppySyncProgressState

data class FollowedShowsUiState(
  val searchQuery: String? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
)
