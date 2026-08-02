package com.michaldrabik.ui_my_shows.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ReloadData
import com.michaldrabik.ui_base.floppy.FloppySyncWorker
import com.michaldrabik.ui_base.floppy.FloppySyncProgressState
import com.michaldrabik.ui_base.floppy.floppySyncProgressState
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedShowsViewModel @Inject constructor(
  private val eventsManager: EventsManager,
  workManager: WorkManager,
) : ViewModel() {

  private val searchQueryState = MutableStateFlow<String?>(null)
  private val syncingState = MutableStateFlow(false)
  private val syncPhaseState = MutableStateFlow<FloppySyncProgressState?>(null)

  init {
    workManager.getWorkInfosByTagLiveData(FloppySyncWorker.TAG_ID).observeForever { work ->
      val running = work.find { it.state == WorkInfo.State.RUNNING }
      syncingState.value = running != null
      syncPhaseState.value = running?.floppySyncProgressState()
    }
  }

  fun onSearchQuery(searchQuery: String?) {
    searchQueryState.value = searchQuery
  }

  fun refreshData() {
    viewModelScope.launch {
      eventsManager.sendEvent(ReloadData)
    }
  }

  val uiState = combine(
    searchQueryState,
    syncingState,
    syncPhaseState,
  ) { s1, s2, s3 ->
    FollowedShowsUiState(
      searchQuery = s1,
      isSyncing = s2,
      syncPhaseState = s3,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = FollowedShowsUiState(),
  )
}
