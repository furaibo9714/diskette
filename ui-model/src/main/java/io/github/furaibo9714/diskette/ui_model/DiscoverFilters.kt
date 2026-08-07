package io.github.furaibo9714.diskette.ui_model

data class DiscoverFilters(
  val feedOrder: DiscoverFeed = DiscoverFeed.TRENDING,
  val hideAnticipated: Boolean = true,
  val hideCollection: Boolean = false,
  val genres: List<Genre> = emptyList(),
  val networks: List<Network> = emptyList(),
)
