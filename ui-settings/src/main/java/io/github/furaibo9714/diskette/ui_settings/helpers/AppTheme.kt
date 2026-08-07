package io.github.furaibo9714.diskette.ui_settings.helpers

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import io.github.furaibo9714.diskette.ui_settings.R

enum class AppTheme(
  val code: Int,
  @StringRes val displayName: Int,
) {
  DARK(MODE_NIGHT_YES, R.string.textThemeDark),
  LIGHT(MODE_NIGHT_NO, R.string.textThemeLight),
  ;

  companion object {
    fun fromCode(code: Int) = values().first { it.code == code }
  }
}
