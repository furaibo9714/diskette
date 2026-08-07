package io.github.furaibo9714.diskette.ui_search.cases

import io.github.furaibo9714.diskette.common.Mode.MOVIES
import io.github.furaibo9714.diskette.common.Mode.SHOWS
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import io.github.furaibo9714.diskette.ui_search.utilities.SearchOptions
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SearchFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  val isMoviesEnabled by lazy { settingsRepository.isMoviesEnabled }

  fun filter(
    searchOptions: SearchOptions,
    item: SearchListItem,
  ): Boolean {
    val filterNone = searchOptions.filters.isEmpty() || searchOptions.filters.containsAll(listOf(SHOWS, MOVIES))
    return when {
      filterNone -> true
      searchOptions.filters.contains(SHOWS) -> item.isShow
      searchOptions.filters.contains(MOVIES) && isMoviesEnabled -> item.isMovie
      searchOptions.filters.contains(MOVIES) && !isMoviesEnabled -> false
      else -> true
    }
  }
}
