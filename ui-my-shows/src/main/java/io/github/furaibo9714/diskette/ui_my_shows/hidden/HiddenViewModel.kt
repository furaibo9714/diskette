package io.github.furaibo9714.diskette.ui_my_shows.hidden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.events.EventsManager
import io.github.furaibo9714.diskette.ui_base.events.ReloadData
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncError
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncProgress
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncSuccess
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.findReplace
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_my_shows.common.recycler.CollectionListItem
import io.github.furaibo9714.diskette.ui_my_shows.common.recycler.CollectionListItem.ShowItem
import io.github.furaibo9714.diskette.ui_my_shows.hidden.cases.HiddenLoadShowsCase
import io.github.furaibo9714.diskette.ui_my_shows.hidden.cases.HiddenSortOrderCase
import io.github.furaibo9714.diskette.ui_my_shows.hidden.cases.HiddenTranslationsCase
import io.github.furaibo9714.diskette.ui_my_shows.main.FollowedShowsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import io.github.furaibo9714.diskette.ui_base.events.Event as EventSync

@HiltViewModel
class HiddenViewModel @Inject constructor(
  private val sortOrderCase: HiddenSortOrderCase,
  private val loadShowsCase: HiddenLoadShowsCase,
  private val translationsCase: HiddenTranslationsCase,
  private val imagesProvider: ShowImagesProvider,
  private val eventsManager: EventsManager,
  private val settingsRepository: SettingsRepository,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<CollectionListItem>>(emptyList())
  private val sortOrderState = MutableStateFlow<Event<Pair<SortOrder, SortType>>?>(null)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)
  private val viewModeState = MutableStateFlow(ListViewMode.LIST_NORMAL)

  private var searchQuery: String? = null

  init {
    viewModelScope.launch { eventsManager.events.collect { onEvent(it) } }
    viewModeState.value = ListViewMode.valueOf(settingsRepository.viewMode.hiddenShowsViewMode)
  }

  fun toggleViewMode() {
    val newMode = viewModeState.value.next()
    settingsRepository.viewMode.hiddenShowsViewMode = newMode.name
    viewModeState.value = newMode
  }

  fun onParentState(state: FollowedShowsUiState) {
    when {
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadShows(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  fun loadShows(resetScroll: Boolean = false) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      itemsState.value = loadShowsCase.loadShows(searchQuery ?: "")
      scrollState.value = Event(resetScroll)
    }
  }

  fun setSortOrder(
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder, sortType)
      loadShows(resetScroll = true)
    }
  }

  fun loadMissingImage(
    item: CollectionListItem,
    force: Boolean,
  ) {
    check(item is ShowItem)
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: CollectionListItem) {
    check(item is ShowItem)
    if (item.translation != null || translationsCase.getLanguage() == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsCase.loadTranslation(item.show, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  private fun updateItem(new: CollectionListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
  }

  private fun onEvent(event: EventSync) =
    when (event) {
      is FloppySyncSuccess -> loadShows()
      is FloppySyncError -> loadShows()
      is FloppySyncProgress -> loadShows()
      is ReloadData -> loadShows()
      else -> Unit
    }

  val uiState = combine(
    itemsState,
    sortOrderState,
    scrollState,
    viewModeState,
  ) { s1, s2, s3, s4 ->
    HiddenUiState(
      items = s1,
      sortOrder = s2,
      resetScroll = s3,
      viewMode = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = HiddenUiState(),
  )
}
