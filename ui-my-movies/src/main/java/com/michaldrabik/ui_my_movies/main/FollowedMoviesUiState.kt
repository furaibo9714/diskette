package com.michaldrabik.ui_my_movies.main

import com.michaldrabik.ui_base.floppy.FloppySyncProgressState

data class FollowedMoviesUiState(
  val searchQuery: String? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseState: FloppySyncProgressState? = null,
)
