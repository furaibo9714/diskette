package io.github.furaibo9714.diskette.ui_lists.manage

import io.github.furaibo9714.diskette.ui_lists.manage.recycler.ManageListsItem

data class ManageListsUiState(
  val items: List<ManageListsItem>? = null,
)
