package io.github.furaibo9714.diskette.ui_people.list

import io.github.furaibo9714.diskette.ui_people.list.recycler.PeopleListItem

data class PeopleListUiState(
  val peopleItems: List<PeopleListItem>? = null,
)
