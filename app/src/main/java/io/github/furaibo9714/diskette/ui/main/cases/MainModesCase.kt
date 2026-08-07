package io.github.furaibo9714.diskette.ui.main.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.Mode.MOVIES
import io.github.furaibo9714.diskette.common.Mode.SHOWS
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainModesCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setMode(mode: Mode) {
    settingsRepository.mode = mode
  }

  fun getMode(): Mode {
    val isMoviesEnabled = settingsRepository.isMoviesEnabled
    val isMovies = settingsRepository.mode == MOVIES
    return if (isMoviesEnabled && isMovies) MOVIES else SHOWS
  }
}
