package io.github.furaibo9714.diskette.ui_settings

import io.github.furaibo9714.diskette.ui_settings.views.SettingsFiltersView

data class SettingsUiState(
  val filter: SettingsFiltersView.SettingsFilter? = null,
)
