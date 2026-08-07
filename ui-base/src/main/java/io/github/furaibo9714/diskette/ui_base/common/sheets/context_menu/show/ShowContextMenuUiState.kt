package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show

import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem

data class ShowContextMenuUiState(
  val isLoading: Boolean? = null,
  val isLoadingSecondary: Boolean? = null,
  val item: ShowContextItem? = null,
)
