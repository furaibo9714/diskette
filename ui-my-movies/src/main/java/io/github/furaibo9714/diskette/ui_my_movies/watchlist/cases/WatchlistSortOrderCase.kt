package io.github.furaibo9714.diskette.ui_my_movies.watchlist.cases

import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    settingsRepository.sorting.watchlistMoviesSortOrder = sortOrder
    settingsRepository.sorting.watchlistMoviesSortType = sortType
  }
}
