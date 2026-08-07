package io.github.furaibo9714.diskette.ui_my_shows.common.filters.network

import io.github.furaibo9714.diskette.ui_model.Network

internal data class CollectionFiltersNetworkUiState(
  val networks: List<Network>? = null,
  val isLoading: Boolean? = null,
)
