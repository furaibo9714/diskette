package io.github.furaibo9714.diskette.ui_my_shows.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.events.Event as EventSync
import io.github.furaibo9714.diskette.ui_base.events.EventsManager
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncError
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncProgress
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncSuccess
import io.github.furaibo9714.diskette.ui_base.events.ReloadData
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.findReplace
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_model.MyShowsSection.ALL
import io.github.furaibo9714.diskette.ui_model.MyShowsSection.RECENTS
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.UserRating
import io.github.furaibo9714.diskette.ui_my_shows.main.FollowedShowsUiState
import io.github.furaibo9714.diskette.ui_my_shows.myshows.cases.MyShowsLoadShowsCase
import io.github.furaibo9714.diskette.ui_my_shows.myshows.cases.MyShowsRatingsCase
import io.github.furaibo9714.diskette.ui_my_shows.myshows.cases.MyShowsSortingCase
import io.github.furaibo9714.diskette.ui_my_shows.myshows.cases.MyShowsTranslationsCase
import io.github.furaibo9714.diskette.ui_my_shows.myshows.recycler.MyShowsItem
import io.github.furaibo9714.diskette.ui_my_shows.myshows.recycler.MyShowsItem.Type
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class MyShowsViewModel @Inject constructor(
  private val loadShowsCase: MyShowsLoadShowsCase,
  private val sortingCase: MyShowsSortingCase,
  private val ratingsCase: MyShowsRatingsCase,
  private val translationsCase: MyShowsTranslationsCase,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val eventsManager: EventsManager,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<MyShowsItem>?>(null)
  private val itemsUpdateState = MutableStateFlow<Event<List<Type>?>?>(null)
  private val viewModeState = MutableStateFlow(ListViewMode.LIST_NORMAL)
  private val showEmptyViewState = MutableStateFlow(false)

  private var searchQuery: String? = null

  init {
    viewModelScope.launch { eventsManager.events.collect { onEvent(it) } }
    viewModeState.value = ListViewMode.valueOf(settingsRepository.viewMode.myShowsViewMode)
  }

  fun toggleViewMode() {
    val newMode = viewModeState.value.next()
    settingsRepository.viewMode.myShowsViewMode = newMode.name
    viewModeState.value = newMode
  }

  fun onParentState(state: FollowedShowsUiState) {
    when {
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        val resetScrolls =
          if (state.searchQuery.isNullOrBlank()) {
            listOf(Type.ALL_SHOWS_ITEM)
          } else {
            emptyList()
          }
        loadShows(resetScroll = resetScrolls)
      }
    }
  }

  fun loadShows(resetScroll: List<Type>? = null) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      val settings = settingsRepository.load()
      val ratings = ratingsCase.loadRatings()
      val sortOrder = settingsRepository.sorting.myShowsAllSortOrder
      val networks = settingsRepository.filters.myShowsNetworks
      val genres = settingsRepository.filters.myShowsGenres
      val spoilers = settingsRepository.spoilers.getAll()

      val shows = loadShowsCase
        .loadAllShows()
        .map {
          toListItemAsync(
            itemType = Type.ALL_SHOWS_ITEM,
            show = it,
            type = POSTER,
            userRating = ratings[it.ids.media],
            sortOrder = sortOrder,
            spoilers = spoilers,
          )
        }.awaitAll()

      val seasons = loadShowsCase.loadSeasonsForShows(shows.map { it.show.mediaId })
      val allShows = loadShowsCase.filterSectionShows(
        allShows = shows,
        allSeasons = seasons,
        searchQuery = searchQuery,
        networks = networks.flatMap { network -> network.channels.map { it } },
        genres = genres.map { it.slug },
      )

      val recentShows = if (settings.myRecentsAmount > 0) {
        loadShowsCase
          .loadRecentShows()
          .map {
            toListItemAsync(Type.RECENT_SHOWS, it, ImageType.FANART, ratings[it.ids.media], null, spoilers)
          }.awaitAll()
      } else {
        emptyList()
      }

      val isNotSearching = searchQuery.isNullOrBlank()
      val listItems = mutableListOf<MyShowsItem>()
      listItems.run {
        if (isNotSearching && recentShows.isNotEmpty()) {
          add(MyShowsItem.createHeader(RECENTS, recentShows.count(), null, null, null))
          add(MyShowsItem.createRecentsSection(recentShows))
        }
        if (shows.isNotEmpty()) {
          add(
            MyShowsItem.createHeader(
              section = settingsRepository.filters.myShowsType,
              itemCount = allShows.count(),
              sortOrder = sortingCase.loadSectionSortOrder(ALL),
              networks = settingsRepository.filters.myShowsNetworks,
              genres = settingsRepository.filters.myShowsGenres,
            ),
          )
          addAll(allShows)
        }
      }

      itemsState.value = listItems
      itemsUpdateState.value = Event(resetScroll)
      showEmptyViewState.value = shows.isEmpty()
    }
  }

  fun setSortOrder(
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    viewModelScope.launch {
      sortingCase.setSectionSortOrder(ALL, sortOrder, sortType)
      loadShows()
    }
  }

  fun loadMissingImage(
    item: MyShowsItem,
    force: Boolean,
  ) {
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

  fun loadMissingTranslation(item: MyShowsItem) {
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

  private fun updateItem(new: MyShowsItem) {
    val items = uiState.value.items?.toMutableList()
    items?.findReplace(new) { it.isSameAs(new) }
    itemsState.value = items
  }

  private fun CoroutineScope.toListItemAsync(
    itemType: Type,
    show: Show,
    type: ImageType = POSTER,
    userRating: UserRating?,
    sortOrder: SortOrder?,
    spoilers: SpoilersSettings,
  ) = async {
    val image = imagesProvider.findCachedImage(show, type)
    val translation = translationsCase.loadTranslation(show, true)
    MyShowsItem(
      type = itemType,
      header = null,
      recentsSection = null,
      show = show,
      image = image,
      isLoading = false,
      translation = translation,
      userRating = userRating?.rating,
      sortOrder = sortOrder,
      spoilers = MyShowsItem.Spoilers(
        isSpoilerHidden = spoilers.isMyShowsHidden,
        isSpoilerRatingsHidden = spoilers.isMyShowsRatingsHidden,
        isSpoilerTapToReveal = spoilers.isTapToReveal,
      ),
    )
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
    itemsUpdateState,
    viewModeState,
    showEmptyViewState,
  ) { s1, s2, s3, s4 ->
    MyShowsUiState(
      items = s1,
      resetScrollMap = s2,
      viewMode = s3,
      showEmptyView = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MyShowsUiState(),
  )
}
