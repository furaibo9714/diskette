package io.github.furaibo9714.diskette.ui_people.list.recycler

import io.github.furaibo9714.diskette.ui_model.Person

sealed class PeopleListItem {

  data class PersonItem(
    val person: Person,
  ) : PeopleListItem()

  data class HeaderItem(
    val department: Person.Department,
    val mediaTitle: String,
  ) : PeopleListItem()
}
