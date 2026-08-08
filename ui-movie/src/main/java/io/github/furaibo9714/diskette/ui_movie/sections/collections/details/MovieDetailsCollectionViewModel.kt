package io.github.furaibo9714.diskette.ui_movie.sections.collections.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.findReplace
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.rethrowCancellation
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases.MovieDetailsCollectionDetailsCase
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases.MovieDetailsCollectionImagesCase
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases.MovieDetailsCollectionMoviesCase
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases.MovieDetailsCollectionTranslationsCase
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.LoadingItem
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.MovieItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsCollectionViewModel @Inject constructor(
  private val collectionDetailsCase: MovieDetailsCollectionDetailsCase,
  private val collectionMoviesCase: MovieDetailsCollectionMoviesCase,
  private val collectionMoviesImagesCase: MovieDetailsCollectionImagesCase,
  private val collectionMoviesTranslationsCase: MovieDetailsCollectionTranslationsCase,
  private val settingsRepository: SettingsRepository,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private val itemsState = MutableStateFlow<MutableList<MovieDetailsCollectionItem>?>(null)

  private var imagesJobs = mutableMapOf<String, Boolean>()
  private var translationsJobs = mutableMapOf<String, Boolean>()

  fun loadCollection(collectionId: MediaId) {
    viewModelScope.launch {
      try {
        val headerItem = collectionDetailsCase.loadCollection(collectionId)
        itemsState.value = mutableListOf(headerItem)
        loadCollectionMovies(collectionId)
      } catch (error: Throwable) {
        // TODO Collection not available
        rethrowCancellation(error)
      }
    }
  }

  private fun loadCollectionMovies(collectionId: MediaId) {
    viewModelScope.launch {
      val loadingJob = launch {
        delay(500)
        itemsState.update {
          it?.toMutableList()?.apply { add(LoadingItem) }
        }
      }
      try {
        val moviesItems = collectionMoviesCase.loadCollectionMovies(
          collectionId = collectionId,
          language = settingsRepository.language,
        )
        itemsState.update {
          it?.toMutableList()?.apply {
            remove(LoadingItem)
            addAll(moviesItems)
          }
        }
      } catch (error: Throwable) {
        // TODO Error
        rethrowCancellation(error)
      } finally {
        loadingJob.cancel()
      }
    }
  }

  fun loadMissingImage(
    item: MovieDetailsCollectionItem,
    force: Boolean,
  ) {
    if (item.id in imagesJobs.keys) {
      return
    }
    imagesJobs[item.id] = true
    viewModelScope.launch {
      (item as? MovieItem)?.let {
        updateItem(it.copy(isLoading = true))
        val updatedItem = collectionMoviesImagesCase.loadMissingImage(it, force)
        updateItem(updatedItem.copy(isLoading = false))
      }
      imagesJobs.remove(item.id)
    }
  }

  fun loadMissingTranslation(item: MovieDetailsCollectionItem) {
    val language = settingsRepository.language
    if (item.id in translationsJobs.keys || language == Config.DEFAULT_LANGUAGE) {
      return
    }
    translationsJobs[item.id] = true
    viewModelScope.launch {
      (item as? MovieItem)?.let {
        val updatedItem = collectionMoviesTranslationsCase.loadMissingTranslation(it, language)
        updateItem(updatedItem.copy(isLoading = false))
      }
      translationsJobs.remove(item.id)
    }
  }

  private fun updateItem(newItem: MovieDetailsCollectionItem) {
    val currentItems = itemsState.value?.toMutableList()
    currentItems?.findReplace(newItem) { it.id == newItem.id }
    itemsState.value = currentItems
  }

  val uiState = combine(
    itemsState,
  ) { s1 ->
    MovieDetailsCollectionUiState(
      items = s1[0],
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = MovieDetailsCollectionUiState(),
  )
}
