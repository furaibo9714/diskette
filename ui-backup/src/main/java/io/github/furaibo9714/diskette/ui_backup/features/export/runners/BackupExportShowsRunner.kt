package io.github.furaibo9714.diskette.ui_backup.features.export.runners

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.dateIsoStringFromMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.repository.OnHoldItemsRepository
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.shows.ratings.ShowsRatingsRepository
import io.github.furaibo9714.diskette.ui_backup.model.BackupEpisode
import io.github.furaibo9714.diskette.ui_backup.model.BackupEpisodeRating
import io.github.furaibo9714.diskette.ui_backup.model.BackupSeason
import io.github.furaibo9714.diskette.ui_backup.model.BackupSeasonRating
import io.github.furaibo9714.diskette.ui_backup.model.BackupShow
import io.github.furaibo9714.diskette.ui_backup.model.BackupShowRating
import io.github.furaibo9714.diskette.ui_backup.model.BackupShows
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class BackupExportShowsRunner @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val ratingsRepository: ShowsRatingsRepository,
) : BackupExportRunner<BackupShows>() {

  override suspend fun run(): BackupShows {
    Timber.d("Initialized.")
    return runExport()
      .also {
        Timber.d("Success.")
      }
  }

  private suspend fun runExport(): BackupShows =
    withContext(dispatchers.IO) {
      val backupShowsCollection = exportShowsCollection()
      val backupShowsProgress = exportShowsProgress()
      val backupEpisodesProgress = exportEpisodesProgress()

      val backupShowsRatings = exportShowsRatings()
      val backupSeasonsRatings = exportSeasonsRatings()
      val backupEpisodesRatings = exportEpisodesRatings()

      BackupShows(
        collectionHistory = backupShowsCollection.collectionHistory,
        collectionWatchlist = backupShowsCollection.collectionWatchlist,
        collectionHidden = backupShowsCollection.collectionHidden,
        progressSeasons = backupEpisodesProgress.progressSeasons,
        progressEpisodes = backupEpisodesProgress.progressEpisodes,
        progressPinned = backupShowsProgress.progressPinned,
        progressOnHold = backupShowsProgress.progressOnHold,
        ratingsShows = backupShowsRatings.ratingsShows,
        ratingsSeasons = backupSeasonsRatings.ratingsSeasons,
        ratingsEpisodes = backupEpisodesRatings.ratingsEpisodes,
      )
    }

  private suspend fun exportShowsCollection(): BackupShows =
    withContext(dispatchers.IO) {
      val myShowsAsync = async { localSource.myShows.getAll() }
      val watchlistShowsAsync = async { localSource.watchlistShows.getAll() }
      val hiddenShowsAsync = async { localSource.archiveShows.getAll() }

      val myShows = myShowsAsync.await()
      val watchlistShows = watchlistShowsAsync.await()
      val hiddenShows = hiddenShowsAsync.await()

      val collectionMyShows = myShows.map {
        BackupShow(
          mediaId = it.mediaId,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }
      val collectionWatchlist = watchlistShows.map {
        BackupShow(
          mediaId = it.mediaId,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }
      val collectionHidden = hiddenShows.map {
        BackupShow(
          mediaId = it.mediaId,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }

      BackupShows(
        collectionHistory = collectionMyShows,
        collectionWatchlist = collectionWatchlist,
        collectionHidden = collectionHidden,
      )
    }

  private suspend fun exportEpisodesProgress(): BackupShows =
    withContext(dispatchers.IO) {
      val watchedEpisodesAsync = async { localSource.episodes.getAllWatched() }
      val watchedSeasonsAsync = async { localSource.seasons.getAllWatched() }

      val watchedEpisodes = watchedEpisodesAsync.await()
      val watchedSeasons = watchedSeasonsAsync.await()

      val seasonsIds = watchedSeasons.map { it.showMediaId }.distinct()
      val shows = localSource.shows.getAllTmdbIds(mediaIds = seasonsIds)

      val progressSeasons = watchedSeasons.map { season ->
        BackupSeason(
          mediaId = season.mediaId,
          showMediaId = season.showMediaId,
          showTmdbId = shows.getOrDefault(season.showMediaId, -1),
          seasonNumber = season.seasonNumber,
        )
      }

      val progressEpisodes = watchedEpisodes.map { episode ->
        BackupEpisode(
          mediaId = episode.mediaId,
          showMediaId = episode.showMediaId,
          showTmdbId = episode.idShowTmdb,
          episodeNumber = episode.episodeNumber,
          seasonNumber = episode.seasonNumber,
          addedAt = episode.lastWatchedAt?.let { dateIsoStringFromMillis(it.toMillis()) },
        )
      }

      BackupShows(
        progressSeasons = progressSeasons,
        progressEpisodes = progressEpisodes,
      )
    }

  private suspend fun exportShowsProgress(): BackupShows =
    withContext(dispatchers.IO) {
      val pinnedIds = pinnedItemsRepository.getAllShows().map { it.key }
      val onHoldIds = onHoldItemsRepository.getAll().map { it.key }
      BackupShows(
        progressPinned = pinnedIds,
        progressOnHold = onHoldIds,
      )
    }

  // Ratings

  private suspend fun exportShowsRatings(): BackupShows =
    withContext(dispatchers.IO) {
      val ratings = ratingsRepository.loadShowsRatings()

      val showsIds = ratings.map { it.mediaId.key }
      val showsTmdbIds = localSource.shows.getAllTmdbIds(mediaIds = showsIds)

      val showsRatings = ratings.map {
        BackupShowRating(
          mediaId = it.mediaId.key,
          tmdbId = showsTmdbIds.getOrDefault(it.mediaId.key, -1),
          rating = it.rating,
          ratedAt = dateIsoStringFromMillis(it.ratedAt.toMillis()),
        )
      }

      BackupShows(
        ratingsShows = showsRatings,
      )
    }

  private suspend fun exportSeasonsRatings(): BackupShows =
    withContext(dispatchers.IO) {
      val ratings = ratingsRepository.loadSeasonsRatings()
      val seasons = localSource.seasons.getAll(ratings.map { it.mediaId })

      val showsIds = seasons.map { it.showMediaId }.distinct()
      val showsTmdbIds = localSource.shows.getAllTmdbIds(mediaIds = showsIds)

      val seasonsRatings = ratings.map { rating ->
        val season = seasons.find { it.mediaId == rating.mediaId }
        val showMediaId = season?.showMediaId.orEmpty()
        val showTmdbId = showsTmdbIds.getOrDefault(showMediaId, -1)

        BackupSeasonRating(
          mediaId = rating.mediaId,
          showMediaId = showMediaId,
          showTmdbId = showTmdbId,
          seasonNumber = rating.seasonNumber ?: -1,
          rating = rating.rating,
          ratedAt = dateIsoStringFromMillis(rating.ratedAt.toMillis()),
        )
      }

      BackupShows(
        ratingsSeasons = seasonsRatings,
      )
    }

  private suspend fun exportEpisodesRatings(): BackupShows =
    withContext(dispatchers.IO) {
      val ratings = ratingsRepository.loadEpisodesRatings()
      val episodes = localSource.episodes.getAll(ratings.map { it.mediaId })

      val episodesRatings = ratings.map { rating ->
        val episode = episodes.find { it.mediaId == rating.mediaId }

        BackupEpisodeRating(
          mediaId = rating.mediaId,
          showMediaId = episode?.showMediaId.orEmpty(),
          showTmdbId = episode?.idShowTmdb ?: -1,
          seasonNumber = rating.seasonNumber ?: -1,
          episodeNumber = rating.episodeNumber ?: -1,
          rating = rating.rating,
          ratedAt = dateIsoStringFromMillis(rating.ratedAt.toMillis()),
        )
      }

      BackupShows(
        ratingsEpisodes = episodesRatings,
      )
    }
}
