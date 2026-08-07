package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.ui_base.R
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.events.FinishUiEvent
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuHiddenCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuLoadItemCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuMyShowsCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuOnHoldCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuPinnedCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases.ShowContextMenuWatchlistCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import io.github.furaibo9714.diskette.ui_base.network.NetworkStatusProvider
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchDelayed
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.rethrowCancellation
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.ImageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ShowContextMenuViewModel @Inject constructor(
  private val loadItemCase: ShowContextMenuLoadItemCase,
  private val myShowsCase: ShowContextMenuMyShowsCase,
  private val watchlistCase: ShowContextMenuWatchlistCase,
  private val hiddenCase: ShowContextMenuHiddenCase,
  private val pinnedCase: ShowContextMenuPinnedCase,
  private val onHoldCase: ShowContextMenuOnHoldCase,
  private val imagesProvider: ShowImagesProvider,
  private val networkProvider: NetworkStatusProvider,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private var showId by notNull<IdTrakt>()

  private val loadingState = MutableStateFlow(false)
  private val loadingSecondaryState = MutableStateFlow(false)
  private val itemState = MutableStateFlow<ShowContextItem?>(null)

  fun loadShow(idTrakt: IdTrakt) {
    viewModelScope.launch {
      showId = idTrakt

      try {
        loadingState.value = true
        val item = loadItemCase.loadItem(idTrakt)
        itemState.value = item
      } catch (error: Throwable) {
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
      } finally {
        loadingState.value = false
      }
    }
  }

  fun moveToMyShows() {
    viewModelScope.launch {
      if (!networkProvider.isOnline()) {
        messageChannel.send(MessageEvent.Error(R.string.errorNoInternetConnection))
        return@launch
      }
      val progressJob = launchDelayed(250) {
        loadingSecondaryState.value = true
      }
      try {
        myShowsCase.moveToMyShows(showId)
        preloadImage()
        finish()
      } catch (error: Throwable) {
        onError(error)
      } finally {
        progressJob.cancel()
      }
    }
  }

  fun removeFromMyShows() {
    viewModelScope.launch {
      try {
        myShowsCase.removeFromMyShows(
          traktId = showId,
          removeLocalData = networkProvider.isOnline(),
        )
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToWatchlist() {
    viewModelScope.launch {
      try {
        watchlistCase.moveToWatchlist(
          traktId = showId,
          removeLocalData = networkProvider.isOnline(),
        )
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromWatchlist() {
    viewModelScope.launch {
      try {
        watchlistCase.removeFromWatchlist(showId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun moveToHidden() {
    viewModelScope.launch {
      try {
        hiddenCase.moveToHidden(
          traktId = showId,
          removeLocalData = networkProvider.isOnline(),
        )
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun removeFromHidden() {
    viewModelScope.launch {
      try {
        hiddenCase.removeFromHidden(showId)
        finish()
      } catch (error: Throwable) {
        onError(error)
      }
    }
  }

  fun addToTopPinned() {
    viewModelScope.launch {
      pinnedCase.addToTopPinned(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromTopPinned() {
    viewModelScope.launch {
      pinnedCase.removeFromTopPinned(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun addToOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.addToOnHold(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  fun removeFromOnHoldPinned() {
    viewModelScope.launch {
      onHoldCase.removeFromOnHold(showId)
      eventChannel.send(Event(FinishUiEvent(true)))
    }
  }

  private suspend fun preloadImage() {
    try {
      val show = itemState.value?.show
      show?.let {
        imagesProvider.loadRemoteImage(it, ImageType.FANART)
      }
    } catch (error: Throwable) {
      Timber.e(error)
      rethrowCancellation(error)
    }
  }

  private suspend fun finish() {
    loadingState.value = false
    loadingSecondaryState.value = false
    eventChannel.send(Event(FinishUiEvent(true)))
  }

  private suspend fun onError(error: Throwable) {
    loadingState.value = false
    loadingSecondaryState.value = false
    messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
    rethrowCancellation(error)
  }

  val uiState = combine(
    loadingState,
    loadingSecondaryState,
    itemState,
  ) { s1, s2, s3 ->
    ShowContextMenuUiState(
      isLoading = s1,
      isLoadingSecondary = s2,
      item = s3,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowContextMenuUiState(),
  )
}
