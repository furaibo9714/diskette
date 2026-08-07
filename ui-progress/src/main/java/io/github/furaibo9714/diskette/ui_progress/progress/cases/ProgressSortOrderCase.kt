package io.github.furaibo9714.diskette.ui_progress.progress.cases

import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(
    sortOrder: SortOrder,
    sortType: SortType,
    newAtTop: Boolean,
  ) {
    settingsRepository.sorting.progressShowsSortOrder = sortOrder
    settingsRepository.sorting.progressShowsSortType = sortType
    settingsRepository.sorting.progressShowsNewAtTop = newAtTop
  }

  fun loadSortOrder() =
    Triple(
      settingsRepository.sorting.progressShowsSortOrder,
      settingsRepository.sorting.progressShowsSortType,
      settingsRepository.sorting.progressShowsNewAtTop,
    )
}
