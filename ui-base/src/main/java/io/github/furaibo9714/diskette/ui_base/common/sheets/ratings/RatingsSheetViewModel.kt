package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.furaibo9714.diskette.common.errors.DisketteError.CoroutineCancellation
import io.github.furaibo9714.diskette.common.errors.DisketteError.UnauthorizedError
import io.github.furaibo9714.diskette.common.errors.ErrorHelper
import io.github.furaibo9714.diskette.ui_base.R
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases.RatingsEpisodeCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases.RatingsMovieCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases.RatingsSeasonCase
import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.cases.RatingsShowCase
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RatingsSheetViewModel @Inject constructor(
  private val showRatingsCase: RatingsShowCase,
  private val movieRatingsCase: RatingsMovieCase,
  private val episodeRatingsCase: RatingsEpisodeCase,
  private val seasonRatingsCase: RatingsSeasonCase,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private val loadingState = MutableStateFlow(false)
  private val ratingState = MutableStateFlow<UserRating?>(null)

  fun loadRating(
    idTrakt: IdTrakt,
    type: Type,
  ) {
    viewModelScope.launch {
      try {
        val rating = when (type) {
          Type.SHOW -> showRatingsCase.loadRating(idTrakt)
          Type.MOVIE -> movieRatingsCase.loadRating(idTrakt)
          Type.EPISODE -> episodeRatingsCase.loadRating(idTrakt)
          Type.SEASON -> seasonRatingsCase.loadRating(idTrakt)
        }
        ratingState.value = rating
      } catch (error: Throwable) {
        handleError(error)
      }
    }
  }

  fun saveRating(
    rating: Int,
    id: IdTrakt,
    type: Type,
    seasonNumber: Int?,
    episodeNumber: Int?,
  ) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        when (type) {
          Type.SHOW -> showRatingsCase.saveRating(id, rating)
          Type.MOVIE -> movieRatingsCase.saveRating(id, rating)
          Type.EPISODE -> episodeRatingsCase.saveRating(id, rating, seasonNumber ?: -1, episodeNumber ?: -1)
          Type.SEASON -> seasonRatingsCase.saveRating(id, rating, seasonNumber ?: -1)
        }
        eventChannel.send(FinishUiEvent(operation = Operation.SAVE))
      } catch (error: Throwable) {
        loadingState.value = false
        handleError(error)
      }
    }
  }

  fun removeRating(
    id: IdTrakt,
    type: Type,
    seasonNumber: Int?,
    episodeNumber: Int?,
  ) {
    viewModelScope.launch {
      try {
        loadingState.value = true
        when (type) {
          Type.SHOW -> showRatingsCase.deleteRating(id)
          Type.MOVIE -> movieRatingsCase.deleteRating(id)
          Type.EPISODE -> episodeRatingsCase.deleteRating(id, seasonNumber ?: -1, episodeNumber ?: -1)
          Type.SEASON -> seasonRatingsCase.deleteRating(id, seasonNumber ?: -1)
        }
        eventChannel.send(FinishUiEvent(operation = Operation.REMOVE))
      } catch (error: Throwable) {
        loadingState.value = false
        handleError(error)
      }
    }
  }

  private suspend fun handleError(error: Throwable) {
    when (ErrorHelper.parse(error)) {
      is CoroutineCancellation -> throw error
      is UnauthorizedError -> messageChannel.send(MessageEvent.Error(R.string.errorTraktAuthorization))
      else -> messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
    }
  }

  val uiState = combine(
    loadingState,
    ratingState,
  ) { s1, s2 ->
    RatingsUiState(
      isLoading = s1,
      rating = s2,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = RatingsUiState(),
  )
}
