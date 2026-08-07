package io.github.furaibo9714.diskette.ui_lists.manage.recycler

import io.github.furaibo9714.diskette.ui_model.CustomList

data class ManageListsItem(
  val list: CustomList,
  val isChecked: Boolean,
  val isEnabled: Boolean,
)
