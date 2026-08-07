package io.github.furaibo9714.diskette.ui_settings.sections.misc.cases

import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsMiscUserCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun getUserId() = settingsRepository.userId
}
