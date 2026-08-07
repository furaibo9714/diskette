package io.github.furaibo9714.diskette.ui_my_shows.main

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState

data class FollowedShowsUiState(
  val searchQuery: String? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
)
