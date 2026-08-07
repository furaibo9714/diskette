package io.github.furaibo9714.diskette.ui_people.details

import io.github.furaibo9714.diskette.ui_people.details.recycler.PersonDetailsItem

data class PersonDetailsUiState(
  val personDetailsItems: List<PersonDetailsItem>? = null,
)
