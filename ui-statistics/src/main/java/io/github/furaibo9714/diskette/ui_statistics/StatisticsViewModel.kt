package io.github.furaibo9714.diskette.ui_statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.combine
import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_statistics.cases.StatisticsLoadRatingsCase
import io.github.furaibo9714.diskette.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import io.github.furaibo9714.diskette.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
  private val ratingsCase: StatisticsLoadRatingsCase,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) : ViewModel() {

  private val mostWatchedShowsState = MutableStateFlow<List<StatisticsMostWatchedItem>?>(null)
  private val mostWatchedTotalCountState = MutableStateFlow<Int?>(null)
  private val totalTimeSpentMinutesState = MutableStateFlow<Int?>(null)
  private val totalWatchedEpisodesState = MutableStateFlow<Int?>(null)
  private val totalWatchedEpisodesShowsState = MutableStateFlow<Int?>(null)
  private val topGenresState = MutableStateFlow<List<Genre>?>(null)
  private val ratingsState = MutableStateFlow<List<StatisticsRatingItem>?>(null)

  private var takeLimit = 5

  fun loadData(
    limit: Int = 0,
    initialDelay: Long = 150L,
  ) {
    takeLimit += limit
    viewModelScope.launch {
      val language = translationsRepository.getLanguage()

      val myShows = showsRepository.myShows.loadAll()
      val watchlistShows = showsRepository.watchlistShows.loadAll() // Add shows from watchlist
      val hiddenShows = showsRepository.hiddenShows.loadAll()

      val shows = (myShows + watchlistShows + hiddenShows).distinctBy { it.mediaId }
      val showsIds = shows.map { it.mediaId }

      val episodes = batchEpisodes(showsIds)
      val seasons = batchSeasons(showsIds)

      val genres = extractTopGenres(shows)
      val mostWatchedShows = shows
        .map { show ->
          val translation = loadTranslation(language, show)
          StatisticsMostWatchedItem(
            show = shows.first { it.mediaId == show.mediaId },
            seasonsCount = seasons.filter { it.showMediaId == show.mediaId.key }.count().toLong(),
            episodes = episodes
              .filter { it.showMediaId == show.mediaId.key }
              .map { mappers.episode.fromDatabase(it) },
            image = Image.createUnknown(POSTER),
            translation = translation,
          )
        }.sortedByDescending { item -> item.episodes.sumOf { it.runtime } }
        .take(takeLimit)
        .map {
          it.copy(image = imagesProvider.findCachedImage(it.show, POSTER))
        }

      delay(initialDelay) // Let transition finish peacefully.

      mostWatchedShowsState.value = mostWatchedShows
      mostWatchedTotalCountState.value = showsIds.size
      totalTimeSpentMinutesState.value = episodes.sumOf { it.runtime }
      totalWatchedEpisodesState.value = episodes.count()
      totalWatchedEpisodesShowsState.value = episodes.distinctBy { it.showMediaId }.count()
      topGenresState.value = genres
    }
  }

  fun loadRatings() {
    viewModelScope.launch {
      try {
        ratingsState.value = ratingsCase.loadRatings()
      } catch (t: Throwable) {
        ratingsState.value = emptyList()
      }
    }
  }

  private suspend fun batchEpisodes(
    showsIds: List<MediaId>,
    allEpisodes: MutableList<Episode> = mutableListOf(),
  ): List<Episode> {
    viewModelScope.ensureActive()

    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = localSource.episodes.getAllWatchedForShows(batch.map { it.key })
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private suspend fun batchSeasons(
    showsIds: List<MediaId>,
    allSeasons: MutableList<Season> = mutableListOf(),
  ): List<Season> {
    viewModelScope.ensureActive()

    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allSeasons

    val seasons = localSource.seasons.getAllWatchedForShows(batch.map { it.key })
    allSeasons.addAll(seasons)

    return batchSeasons(showsIds.filter { it !in batch }, allSeasons)
  }

  private fun extractTopGenres(shows: List<Show>) =
    shows
      .flatMap { it.genres }
      .asSequence()
      .filter { it.isNotBlank() }
      .distinct()
      .map { genre -> Pair(Genre.fromSlug(genre), shows.count { genre in it.genres }) }
      .sortedByDescending { it.second }
      .map { it.first }
      .toList()
      .filterNotNull()

  private suspend fun loadTranslation(
    language: String,
    show: Show,
  ) = if (language == Config.DEFAULT_LANGUAGE) {
    null
  } else {
    translationsRepository.loadTranslation(show, language, true)
  }

  val uiState = combine(
    mostWatchedShowsState,
    mostWatchedTotalCountState,
    totalTimeSpentMinutesState,
    totalWatchedEpisodesState,
    totalWatchedEpisodesShowsState,
    topGenresState,
    ratingsState,
  ) { s1, s2, s3, s4, s5, s6, s7 ->
    StatisticsUiState(
      mostWatchedShows = s1,
      mostWatchedTotalCount = s2,
      totalTimeSpentMinutes = s3,
      totalWatchedEpisodes = s4,
      totalWatchedEpisodesShows = s5,
      topGenres = s6,
      ratings = s7,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = StatisticsUiState(),
  )
}
