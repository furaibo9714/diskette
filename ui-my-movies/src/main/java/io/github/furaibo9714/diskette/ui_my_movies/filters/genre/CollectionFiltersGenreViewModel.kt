package io.github.furaibo9714.diskette.ui_my_movies.filters.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersOrigin
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersOrigin.HIDDEN_MOVIES
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersOrigin.MY_MOVIES
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersOrigin.WATCHLIST_MOVIES
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersUiEvent.ApplyFilters
import io.github.furaibo9714.diskette.ui_my_movies.filters.CollectionFiltersUiEvent.CloseFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CollectionFiltersGenreViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private val genresState = MutableStateFlow<List<Genre>?>(null)
  private val loadingState = MutableStateFlow(false)

  private lateinit var origin: CollectionFiltersOrigin

  fun loadData(origin: CollectionFiltersOrigin) {
    this.origin = origin
    genresState.value = when (origin) {
      MY_MOVIES -> settingsRepository.filters.myMoviesGenres
      WATCHLIST_MOVIES -> settingsRepository.filters.watchlistMoviesGenres
      HIDDEN_MOVIES -> settingsRepository.filters.hiddenMoviesGenres
    }.toList()
  }

  fun saveGenres(genres: List<Genre>) {
    viewModelScope.launch {
      if (genres == genresState.value) {
        eventChannel.send(CloseFilters)
        return@launch
      }
      when (origin) {
        MY_MOVIES -> settingsRepository.filters.myMoviesGenres = genres
        WATCHLIST_MOVIES -> settingsRepository.filters.watchlistMoviesGenres = genres
        HIDDEN_MOVIES -> settingsRepository.filters.hiddenMoviesGenres = genres
      }
      eventChannel.send(ApplyFilters)
    }
  }

  val uiState = combine(
    genresState,
    loadingState,
  ) { s1, s2 ->
    CollectionFiltersGenreUiState(
      genres = s1,
      isLoading = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CollectionFiltersGenreUiState(),
  )
}
