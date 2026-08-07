package io.github.furaibo9714.diskette.ui_my_movies.mymovies.cases

import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyMoviesSortingCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadSortOrder() =
    Pair(
      settingsRepository.sorting.myMoviesAllSortOrder,
      settingsRepository.sorting.myMoviesAllSortType,
    )

  fun setSortOrder(
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    settingsRepository.sorting.myMoviesAllSortOrder = sortOrder
    settingsRepository.sorting.myMoviesAllSortType = sortType
  }
}
