package io.github.furaibo9714.diskette.ui_episodes.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.EpisodeImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.combine
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_episodes.details.cases.EpisodeDetailsSeasonCase
import io.github.furaibo9714.diskette.ui_episodes.details.cases.EpisodeDetailsWatchedCase
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.RatingState
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
  settingsSpoilersRepository: SettingsSpoilersRepository,
  private val seasonsCase: EpisodeDetailsSeasonCase,
  private val watchedCase: EpisodeDetailsWatchedCase,
  private val imagesProvider: EpisodeImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val ratingsRepository: RatingsRepository,
  private val translationsRepository: TranslationsRepository,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private val imageState = MutableStateFlow<Image?>(null)
  private val imageLoadingState = MutableStateFlow(false)
  private val episodesState = MutableStateFlow<List<Episode>?>(null)
  private val ratingState = MutableStateFlow<RatingState?>(null)
  private val translationState = MutableStateFlow<Translation?>(null)
  private val lastWatchedAtState = MutableStateFlow<ZonedDateTime?>(null)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)
  private val spoilersState = MutableStateFlow<SpoilersSettings?>(null)

  init {
    dateFormatState.value = dateFormatProvider.loadFullHourFormat()
    spoilersState.value = settingsSpoilersRepository.getAll()
  }

  fun loadLastWatchedAt(
    showTraktId: MediaId,
    episode: Episode,
  ) {
    viewModelScope.launch {
      val lastWatchedAt = watchedCase.getLastWatchedAt(showTraktId, episode)
      lastWatchedAtState.update { lastWatchedAt }
    }
  }

  fun loadImage(
    showId: IdTmdb,
    episode: Episode,
  ) {
    viewModelScope.launch {
      try {
        imageLoadingState.value = true
        val episodeImage = imagesProvider.loadRemoteImage(showId, episode)
        imageState.value = episodeImage
        imageLoadingState.value = false
      } catch (t: Throwable) {
        imageLoadingState.value = false
      }
    }
  }

  fun loadSeason(
    showTraktId: MediaId,
    episode: Episode,
    seasonEpisodes: IntArray?,
  ) {
    viewModelScope.launch {
      val episodes = seasonsCase.loadSeason(showTraktId, episode, seasonEpisodes)
      if (episodes.isNotEmpty()) {
        delay(100)
      }
      episodesState.value = episodes
    }
  }

  fun loadTranslation(
    showTraktId: MediaId,
    episode: Episode,
  ) {
    viewModelScope.launch {
      try {
        val language = translationsRepository.getLanguage()
        if (language == Config.DEFAULT_LANGUAGE) {
          return@launch
        }
        val translation = translationsRepository.loadTranslation(episode, showTraktId, language)
        translation?.let {
          translationState.value = it
        }
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  fun loadRatings(episode: Episode) {
    viewModelScope.launch {
      try {
        ratingState.value = RatingState(rateLoading = true)
        val rating = ratingsRepository.shows.loadRating(episode)
        ratingState.value = RatingState(rateLoading = false, userRating = rating)
      } catch (error: Throwable) {
        ratingState.value = RatingState(rateLoading = false)
      }
    }
  }

  val uiState = combine(
    imageState,
    imageLoadingState,
    episodesState,
    ratingState,
    translationState,
    dateFormatState,
    spoilersState,
    lastWatchedAtState,
  ) { s1, s2, s3, s4, s5, s6, s7, s8 ->
    EpisodeDetailsUiState(
      image = s1,
      isImageLoading = s2,
      episodes = s3,
      rating = s4,
      translation = s5,
      dateFormat = s6,
      spoilers = s7,
      lastWatchedAt = s8,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = EpisodeDetailsUiState(),
  )
}
