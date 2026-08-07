package io.github.furaibo9714.diskette.ui_base.common.sheets.links

import androidx.lifecycle.ViewModel
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.common.AppCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LinksViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel() {

  fun loadCountry() = AppCountry.fromCode(settingsRepository.country)
}
