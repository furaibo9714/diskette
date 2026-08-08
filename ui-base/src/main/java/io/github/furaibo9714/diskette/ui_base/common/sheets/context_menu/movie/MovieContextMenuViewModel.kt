package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.R
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.events.FinishUiEvent
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.events.SelectDateUiEvent
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuHiddenCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuLoadItemCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuMyMoviesCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuPinnedCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases.MovieContextMenuWatchlistCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.rethrowCancellation
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ProgressDateSelectionType.ALWAYS_ASK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MovieContextMenuViewModel @Inject constructor(
  private val loadItemCase: MovieContextMenuLoadItemCase,
  private val myMoviesCase: MovieContextMenuMyMoviesCase,
  private val watchlistCase: MovieContextMenuWatchlistCase,
  private val hiddenCase: MovieContextMenuHiddenCase,
  private val pinnedCase: MovieContextMenuPinnedCase,
  private val settingsRepository: SettingsRepository,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private var movieId by notNull<MediaId>()

  private val loadingState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<MovieContextItem?>(null)

  fun loadMovie(mediaId: MediaId) {
    viewModelScope.launch {
      movieId = mediaId

      try {
        loadingState.value = true
        val item = loadItemCase.loadItem(mediaId)
        itemState.value = item
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
      } finally {
        loadingState.value = false
      }
    }
  }

  fun moveToMyMovies(
    isCustomDateSelected: Boolean = false,
    customDate: ZonedDateTime? = null,
  ) {
    viewModelScope.launch {
      try {
        val movie = itemState.value?.movie
        val isCustomDateAlwaysAsk = { settingsRepository.progressDateSelectionType == ALWAYS_ASK }
        if (movie != null && !isCustomDateSelected && isCustomDateAlwaysAsk()) {
          eventChannel.send(Event(SelectDateUiEvent(movie)))
          return@launch
        }
        myMoviesCase.moveToMyMovies(movieId, customDate)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromMyMovies() {
    viewModelScope.launch {
      try {
        myMoviesCase.removeFromMyMovies(movieId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        watchlistCase.moveToWatchlist(movieId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromWatchlist() {
    viewModelScope.launch {
      try {
        watchlistCase.removeFromWatchlist(movieId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToHidden() {
    viewModelScope.launch {
      try {
        hiddenCase.moveToHidden(movieId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromHidden() {
    viewModelScope.launch {
      try {
        hiddenCase.removeFromHidden(movieId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun addToTopPinned() {
    viewModelScope.launch {
      pinnedCase.addToTopPinned(movieId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(movieId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  private suspend fun finish() {
    loadingState.value = false
    eventChannel.send(Event(FinishUiEvent(true)))
  }

  private suspend fun onError(error: Throwable) {
    loadingState.value = false
    messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
    rethrowCancellation(error)
  }

  val uiState = combine(
    loadingState,
    itemState,
  ) { s1, s2 ->
    MovieContextMenuUiState(
      isLoading = s1,
      item = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieContextMenuUiState(),
  )
}
