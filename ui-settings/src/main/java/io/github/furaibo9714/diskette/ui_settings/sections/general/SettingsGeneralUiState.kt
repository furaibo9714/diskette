package io.github.furaibo9714.diskette.ui_settings.sections.general

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.ui_base.common.AppCountry
import io.github.furaibo9714.diskette.ui_base.dates.AppDateFormat
import io.github.furaibo9714.diskette.ui_model.ProgressDateSelectionType
import io.github.furaibo9714.diskette.ui_model.ProgressNextEpisodeType
import io.github.furaibo9714.diskette.ui_model.Settings
import io.github.furaibo9714.diskette.ui_settings.helpers.AppLanguage
import io.github.furaibo9714.diskette.ui_settings.helpers.AppTheme

data class SettingsGeneralUiState(
  val settings: Settings? = null,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val theme: AppTheme = AppTheme.DARK,
  val country: AppCountry? = null,
  val dateFormat: AppDateFormat? = null,
  val moviesEnabled: Boolean = true,
  val newsEnabled: Boolean = false,
  val streamingsEnabled: Boolean = true,
  val restartApp: Boolean = false,
  val progressNextType: ProgressNextEpisodeType? = null,
  val progressDateSelectionType: ProgressDateSelectionType? = null,
  val progressUpcomingDays: Long? = null,
  val tabletColumns: Int = Config.DEFAULT_LISTS_GRID_SPAN,
)
