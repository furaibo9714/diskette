package io.github.furaibo9714.diskette.ui_my_movies.main

import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncProgressState

data class FollowedMoviesUiState(
  val searchQuery: String? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
)
