package io.github.furaibo9714.diskette.ui_show.sections.seasons.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_base.network.NetworkStatusProvider
import io.github.furaibo9714.diskette.ui_model.RatingState
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_show.episodes.recycler.EpisodeListItem
import io.github.furaibo9714.diskette.ui_show.sections.seasons.helpers.SeasonsBundle
import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@ViewModelScoped
class ShowDetailsLoadSeasonsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val episodesManager: EpisodesManager,
  private val dateFormatProvider: DateFormatProvider,
  private val networkStatusProvider: NetworkStatusProvider,
) {

  suspend fun loadSeasons(show: Show): SeasonsBundle =
    withContext(dispatchers.IO) {
      val showSpecialSeasons = settingsRepository.load().specialSeasonsEnabled
      try {
        if (!networkStatusProvider.isOnline()) {
          loadLocalSeasons(show, showSpecialSeasons)
        }

        val remoteSeasons = remoteSource.media
          .fetchSeasons(show.ids.tmdb.id)
          .map { mappers.season.fromNetwork(it) }
          .filter { it.episodes.isNotEmpty() }
          .filter { if (!showSpecialSeasons) !it.isSpecial() else true }

        val isFollowed = showsRepository.myShows.load(show.ids.media) != null
        if (isFollowed) {
          episodesManager.invalidateSeasons(show, remoteSeasons)
        }

        val seasonsItems = mapToSeasonItems(remoteSeasons, show)
        SeasonsBundle(seasonsItems, isLocal = false)
      } catch (error: Throwable) {
        loadLocalSeasons(show, showSpecialSeasons)
      }
    }

  private suspend fun loadLocalSeasons(
    show: Show,
    showSpecials: Boolean,
  ): SeasonsBundle {
    val localEpisodes = localSource.episodes.getAllByShowId(show.mediaId.key)
    val localSeasons = localSource.seasons
      .getAllByShowId(show.mediaId.key)
      .map { season ->
        val seasonEpisodes = localEpisodes.filter { ep -> ep.idSeason == season.mediaId }
        mappers.season.fromDatabase(season, seasonEpisodes)
      }.filter { it.episodes.isNotEmpty() }
      .filter { if (!showSpecials) !it.isSpecial() else true }

    val seasonsItems = mapToSeasonItems(localSeasons, show)
    return SeasonsBundle(seasonsItems, isLocal = true)
  }

  private suspend fun mapToSeasonItems(
    remoteSeasons: List<Season>,
    show: Show,
  ) = coroutineScope {
    val format = dateFormatProvider.loadFullHourFormat()
    val seasonsRatings = ratingsRepository.shows.loadRatingsSeasons(remoteSeasons)
    val spoilers = settingsRepository.spoilers.getAll()
    remoteSeasons
      .map {
        val userRating = RatingState(
          userRating = seasonsRatings.find { rating -> rating.mediaId == it.ids.media },
        )
        val episodes = it.episodes
          .map { episode ->
            async {
              val rating = ratingsRepository.shows.loadRating(episode)
              val translation = translationsRepository.loadTranslation(episode, show.ids.media, onlyLocal = true)
              EpisodeListItem(
                episode = episode,
                season = it,
                isWatched = false,
                translation = translation,
                myRating = rating,
                dateFormat = format,
                isAnime = show.isAnime,
                spoilers = spoilers,
              )
            }
          }.awaitAll()
        SeasonListItem(
          show = show,
          season = it,
          episodes = episodes,
          isWatched = false,
          userRating = userRating,
          updatedAt = nowUtcMillis(),
          isRatingHidden = spoilers.isEpisodeRatingHidden,
          isRatingTapToReveal = spoilers.isTapToReveal,
        )
      }.sortedByDescending { it.season.number }
  }
}
