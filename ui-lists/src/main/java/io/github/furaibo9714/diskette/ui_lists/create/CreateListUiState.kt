package io.github.furaibo9714.diskette.ui_lists.create

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.CustomList

data class CreateListUiState(
  val listDetails: CustomList? = null,
  val isLoading: Boolean? = null,
  val onListUpdated: Event<CustomList>? = null,
)
