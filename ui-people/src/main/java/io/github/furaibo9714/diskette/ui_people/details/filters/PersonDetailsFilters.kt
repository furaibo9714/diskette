package io.github.furaibo9714.diskette.ui_people.details.filters

import io.github.furaibo9714.diskette.common.Mode

data class PersonDetailsFilters(
  val modes: List<Mode> = emptyList(),
  val onlyCollection: Boolean = false,
)
