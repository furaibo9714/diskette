package io.github.furaibo9714.diskette.ui_settings.sections.widgets

import io.github.furaibo9714.diskette.ui_model.Settings
import io.github.furaibo9714.diskette.ui_settings.helpers.AppTheme
import io.github.furaibo9714.diskette.ui_settings.helpers.WidgetTransparency

data class SettingsWidgetsUiState(
  val settings: Settings? = null,
  val themeWidgets: AppTheme? = AppTheme.DARK,
  val widgetsTransparency: WidgetTransparency = WidgetTransparency.SOLID,
)
