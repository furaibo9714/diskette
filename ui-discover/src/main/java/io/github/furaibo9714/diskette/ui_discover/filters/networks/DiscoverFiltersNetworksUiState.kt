package io.github.furaibo9714.diskette.ui_discover.filters.networks

import io.github.furaibo9714.diskette.ui_model.Network

internal data class DiscoverFiltersNetworksUiState(
  val networks: List<Network>? = null,
  val isLoading: Boolean? = null,
)
