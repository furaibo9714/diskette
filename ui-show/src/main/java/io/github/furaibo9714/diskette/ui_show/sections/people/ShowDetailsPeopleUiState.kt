package io.github.furaibo9714.diskette.ui_show.sections.people

import io.github.furaibo9714.diskette.ui_model.Person

data class ShowDetailsPeopleUiState(
  val isLoading: Boolean = true,
  val actors: List<Person>? = null,
  val crew: Map<Person.Department, List<Person>>? = null,
)
