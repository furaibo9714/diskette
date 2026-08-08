package io.github.furaibo9714.diskette.repository

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.common.extensions.toUtcZone
import io.github.furaibo9714.diskette.data_local.database.model.EpisodesSyncLog
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.SeasonsLocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.EpisodeBundle
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.SeasonBundle
import io.github.furaibo9714.diskette.ui_model.Show
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import io.github.furaibo9714.diskette.data_local.database.model.Episode as EpisodeDb
import io.github.furaibo9714.diskette.data_local.database.model.Season as SeasonDb

@Singleton
class EpisodesManager @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesLocalSource: EpisodesLocalDataSource,
  private val seasonsLocalSource: SeasonsLocalDataSource,
  private val syncLogLocalSource: EpisodesSyncLogLocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun getWatchedSeasonsIds(show: Show) = seasonsLocalSource.getAllWatchedIdsForShows(listOf(show.mediaId.key))

  suspend fun getWatchedEpisodesIds(show: Show) = episodesLocalSource.getAllWatchedIdsForShows(listOf(show.mediaId.key))

  suspend fun setSeasonWatched(
    seasonBundle: SeasonBundle,
    customDate: ZonedDateTime?,
  ): List<Episode> {
    val date = customDate?.toUtcZone() ?: nowUtc()
    val toAdd = mutableListOf<EpisodeDb>()
    transactions.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.media, true)
      val localSeason = seasonsLocalSource.getById(season.ids.media.id)
      if (localSeason == null) {
        seasonsLocalSource.upsert(listOf(dbSeason))
      }

      val watchedEpisodes = episodesLocalSource.getAllForSeason(season.ids.media.id).filter { it.isWatched }
      season.episodes.forEach { ep ->
        if (watchedEpisodes.none { it.mediaId == ep.ids.media.id }) {
          val dbEpisode = mappers.episode.toDatabase(ep, season, show.ids.media, true, null, date)
          toAdd.add(dbEpisode)
        }
      }

      episodesLocalSource.upsert(toAdd)
      seasonsLocalSource.update(listOf(dbSeason))
      showsRepository.myShows.updateWatchedAt(show.mediaId, date.toMillis())
    }
    return toAdd.map { mappers.episode.fromDatabase(it) }
  }

  suspend fun setSeasonUnwatched(seasonBundle: SeasonBundle) {
    transactions.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.media, false)
      val watchedEpisodes = episodesLocalSource.getAllForSeason(season.ids.media.id).filter { it.isWatched }
      val toSet = watchedEpisodes.map { it.copy(isWatched = false, lastExportedAt = null, lastWatchedAt = null) }

      val isShowFollowed = showsRepository.myShows.load(show.ids.media) != null

      when {
        isShowFollowed -> {
          episodesLocalSource.upsert(toSet)
          seasonsLocalSource.update(listOf(dbSeason))
        }
        else -> {
          episodesLocalSource.delete(toSet)
          seasonsLocalSource.delete(listOf(dbSeason))
        }
      }
    }
  }

  suspend fun setEpisodeWatched(
    episodeId: Long,
    seasonId: Long,
    showId: MediaId,
    customDate: ZonedDateTime?,
  ) {
    val episodeDb = episodesLocalSource.getAllForSeason(seasonId).find { it.mediaId == episodeId }!!
    val seasonDb = seasonsLocalSource.getById(seasonId)!!
    val show = showsRepository.myShows.load(showId)!!
    setEpisodeWatched(
      episodeBundle = EpisodeBundle(
        episode = mappers.episode.fromDatabase(episodeDb),
        season = mappers.season.fromDatabase(seasonDb),
        show = show,
      ),
      customDate = customDate,
    )
  }

  suspend fun setEpisodeWatched(
    episodeBundle: EpisodeBundle,
    customDate: ZonedDateTime?,
  ) {
    transactions.withTransaction {
      val (episode, season, show) = episodeBundle
      val date = customDate?.toUtcZone() ?: nowUtc()

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.media, true, null, date)
      val dbSeason = mappers.season.toDatabase(season, show.ids.media, false)

      val localSeason = seasonsLocalSource.getById(season.ids.media.id)
      if (localSeason == null) {
        seasonsLocalSource.upsert(listOf(dbSeason))
      }
      episodesLocalSource.upsert(listOf(dbEpisode))
      showsRepository.myShows.updateWatchedAt(show.mediaId, date.toMillis())
      onEpisodeSet(season, show)
    }
  }

  suspend fun setEpisodeUnwatched(episodeBundle: EpisodeBundle) {
    transactions.withTransaction {
      val (episode, season, show) = episodeBundle

      val isShowFollowed = showsRepository.myShows.load(show.ids.media) != null
      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.media, true, null, episode.lastWatchedAt)

      when {
        isShowFollowed -> {
          val ep = dbEpisode.copy(
            isWatched = false,
            lastExportedAt = null,
            lastWatchedAt = null,
          )
          episodesLocalSource.upsert(listOf(ep))
        }
        else -> {
          episodesLocalSource.delete(listOf(dbEpisode))
        }
      }

      onEpisodeSet(season, show)
    }
  }

  suspend fun setAllUnwatched(
    showId: MediaId,
    skipSpecials: Boolean = false,
  ) {
    transactions.withTransaction {
      val watchedEpisodes = episodesLocalSource.getAllByShowId(showId.id)
      val watchedSeasons = seasonsLocalSource.getAllByShowId(showId.id)

      val updateEpisodes = watchedEpisodes
        .filter { if (skipSpecials) it.seasonNumber > 0 else true }
        .map { it.copy(isWatched = false, lastExportedAt = null, lastWatchedAt = null) }
      val updateSeasons = watchedSeasons
        .filter { if (skipSpecials) it.seasonNumber > 0 else true }
        .map { it.copy(isWatched = false) }

      episodesLocalSource.upsert(updateEpisodes)
      seasonsLocalSource.update(updateSeasons)
    }
  }

  @Suppress("UNCHECKED_CAST")
  suspend fun invalidateSeasons(
    show: Show,
    remoteSeasons: List<Season>,
  ) {
    if (remoteSeasons.isEmpty()) {
      return
    }
    coroutineScope {
      val (localSeasons, localEpisodes) = awaitAll(
        async { seasonsLocalSource.getAllByShowId(show.mediaId.key) },
        async { episodesLocalSource.getAllByShowId(show.mediaId.key) },
      )
      localSeasons as List<SeasonDb>
      localEpisodes as List<EpisodeDb>

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      remoteSeasons.forEach { remoteSeason ->
        var isAnyEpisodeUnwatched = false

        remoteSeason.episodes.forEach { remoteEpisode ->
          var localEpisode = localEpisodes.find {
            it.episodeNumber == remoteEpisode.number &&
              it.seasonNumber == remoteEpisode.season
          }
          if (localEpisode == null) {
            // Double check by Trakt ID as season/episode combination might be old.
            localEpisode = localEpisodes.find {
              it.mediaId == remoteEpisode.ids.media.id
            }
          }

          val isWatched = localEpisode?.isWatched ?: false
          if (!isWatched) {
            isAnyEpisodeUnwatched = true
          }

          val episodeDb = mappers.episode.toDatabase(
            episode = remoteEpisode,
            season = remoteSeason,
            showId = show.ids.media,
            isWatched = isWatched,
            lastExportedAt = localEpisode?.lastExportedAt,
            lastWatchedAt = localEpisode?.lastWatchedAt,
          )
          episodesToAdd.add(episodeDb)
        }

        val seasonDb = mappers.season.toDatabase(
          season = remoteSeason,
          showId = show.ids.media,
          isWatched = !isAnyEpisodeUnwatched,
        )
        seasonsToAdd.add(seasonDb)
      }

      transactions.withTransaction {
        episodesLocalSource.deleteAllForShow(show.mediaId.key)
        seasonsLocalSource.deleteAllForShow(show.mediaId.key)

        seasonsLocalSource.upsert(seasonsToAdd)
        episodesLocalSource.upsertChunked(episodesToAdd)

        syncLogLocalSource.upsert(EpisodesSyncLog(show.mediaId.key, nowUtcMillis()))
      }

      Timber.d("Episodes updated: ${episodesToAdd.size} Seasons updated: ${seasonsToAdd.size}")
    }
  }

  private suspend fun onEpisodeSet(
    season: Season,
    show: Show,
  ) {
    val localEpisodes = episodesLocalSource.getAllForSeason(season.ids.media.id)
    val isWatched = localEpisodes.count { it.isWatched } == season.episodeCount
    val dbSeason = mappers.season.toDatabase(season, show.ids.media, isWatched)
    seasonsLocalSource.update(listOf(dbSeason))
  }
}
