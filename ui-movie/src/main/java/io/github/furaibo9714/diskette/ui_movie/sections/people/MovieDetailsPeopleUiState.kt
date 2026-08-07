package io.github.furaibo9714.diskette.ui_movie.sections.people

import io.github.furaibo9714.diskette.ui_model.Person

data class MovieDetailsPeopleUiState(
  val isLoading: Boolean = true,
  val actors: List<Person>? = null,
  val crew: Map<Person.Department, List<Person>>? = null,
)
