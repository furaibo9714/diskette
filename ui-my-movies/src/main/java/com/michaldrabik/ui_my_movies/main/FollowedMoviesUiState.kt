package com.michaldrabik.ui_my_movies.main

data class FollowedMoviesUiState(
  val searchQuery: String? = null,
  val isSyncing: Boolean? = null,
  val syncPhaseTextRes: Int? = null,
)
