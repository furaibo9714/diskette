package com.michaldrabik.ui_settings.sections.floppy

data class SettingsFloppyUiState(
  val isConfigured: Boolean = false,
  val isTesting: Boolean = false,
  val baseUrl: String = "",
)
