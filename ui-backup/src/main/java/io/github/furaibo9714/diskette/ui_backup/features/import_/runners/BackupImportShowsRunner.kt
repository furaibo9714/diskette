package io.github.furaibo9714.diskette.ui_backup.features.import_.runners

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.common.extensions.toUtcDateTime
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.MyShow
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.OnHoldItemsRepository
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.repository.shows.ratings.ShowsRatingsRepository
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Importing
import io.github.furaibo9714.diskette.ui_backup.model.BackupShow
import io.github.furaibo9714.diskette.ui_backup.model.BackupShows
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.rethrowCancellation
import io.github.furaibo9714.diskette.ui_model.MediaId
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

internal class BackupImportShowsRunner @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val remoteSource: RemoteDataSource,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val ratingsRepository: ShowsRatingsRepository,
  private val episodesManager: EpisodesManager,
  private val mappers: Mappers,
  private val transactions: TransactionsProvider,
) : BackupImportRunner<BackupShows>() {

  private var importedCount = 0
  private var importedTotal = 0

  override suspend fun run(backup: BackupShows) {
    Timber.d("Initialized.")
    runImport(backup)
      .also {
        Timber.d("Success.")
      }
  }

  private suspend fun runImport(backup: BackupShows) {
    withContext(dispatchers.IO) {
      importShowsCollection(backup)

      importShowsPinned(backup)
      importShowsOnHold(backup)

      importShowsRatings(backup)
      importSeasonsRatings(backup)
      importEpisodesRatings(backup)
    }
  }

  private suspend fun importShowsCollection(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localCollection = showsRepository
        .loadCollection()
        .map { it.mediaId }

      importedCount = 0
      importedTotal = backup.collectionHistory.size + backup.collectionWatchlist.size + backup.collectionHidden.size

      importMyShows(backup, localCollection)
      importWatchlistShows(backup, localCollection)
      importHiddenShows(backup, localCollection)
    }
  }

  private suspend fun importMyShows(
    backupShows: BackupShows,
    localCollection: List<Long>,
  ) {
    for (show in backupShows.collectionHistory) {
      Timber.d("Importing show ${show.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(show.title, importedCount, importedTotal))

      if (localCollection.contains(show.mediaId)) {
        if (showsRepository.myShows.exists(MediaId.parse(show.mediaId))) {
          importExistingMyShowEpisodes(MediaId.parse(show.mediaId), backupShows)
          continue
        }
        Timber.d("Show already in collection. Skipping.")
        continue
      }

      val showDetails = localSource.shows.getById(show.mediaId)
      if (showDetails == null) {
        if (!fetchShowDetails(show)) {
          continue
        }
      }

      val addedAt = show.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val updatedAt = show.updatedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val myShows = MyShow.fromMediaId(
        mediaId = show.mediaId,
        createdAt = addedAt,
        updatedAt = addedAt,
        watchedAt = updatedAt,
      )

      Timber.d("New show in My Shows. Importing season, episodes ...")
      val (seasons, episodes) = loadSeasonsEpisodes(show.mediaId, backupShows)

      transactions.withTransaction {
        localSource.seasons.upsert(seasons)
        localSource.episodes.upsert(episodes)
        localSource.myShows.insert(listOf(myShows))
      }

      Timber.d("Added to My Shows ${show.mediaId} ...")
    }
  }

  private suspend fun importWatchlistShows(
    backupShows: BackupShows,
    localCollection: List<Long>,
  ) {
    for (show in backupShows.collectionWatchlist) {
      Timber.d("Importing show ${show.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(show.title, importedCount, importedTotal))

      if (localCollection.contains(show.mediaId)) {
        Timber.d("Show already in collection. Skipping.")
        continue
      }

      val showDetails = localSource.shows.getById(show.mediaId)
      if (showDetails == null) {
        if (!fetchShowDetails(show)) {
          continue
        }
      }

      val timestamp = show.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val watchlistShow = WatchlistShow.fromMediaId(show.mediaId, timestamp)
      localSource.watchlistShows.insert(watchlistShow)

      Timber.d("Added to Watchlist ${show.mediaId} ...")
    }
  }

  private suspend fun importHiddenShows(
    backupShows: BackupShows,
    localCollection: List<Long>,
  ) {
    for (show in backupShows.collectionHidden) {
      Timber.d("Importing show ${show.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(show.title, importedCount, importedTotal))

      if (localCollection.contains(show.mediaId)) {
        Timber.d("Show already in collection. Skipping.")
        continue
      }

      val showDetails = localSource.shows.getById(show.mediaId)
      if (showDetails == null) {
        if (!fetchShowDetails(show)) {
          continue
        }
      }

      val timestamp = show.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val hiddenShow = ArchiveShow.fromMediaId(show.mediaId, timestamp)
      localSource.archiveShows.insert(hiddenShow)

      Timber.d("Added to Hidden ${show.mediaId} ...")
    }
  }

  private suspend fun importShowsPinned(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localPinned = pinnedItemsRepository.getAllShows()
      for (pinned in backup.progressPinned) {
        if (!localPinned.contains(pinned)) {
          pinnedItemsRepository.addShowPinnedItem(MediaId.parse(pinned))
        }
      }
    }
  }

  private suspend fun importShowsOnHold(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localOnHold = onHoldItemsRepository.getAll().map { it.id }
      for (onHoldShow in backup.progressOnHold) {
        if (!localOnHold.contains(onHoldShow)) {
          onHoldItemsRepository.addItem(MediaId.parse(onHoldShow))
        }
      }
    }
  }

  private suspend fun importShowsRatings(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localRatings = ratingsRepository.loadShowsRatings()

      for (rating in backup.ratingsShows) {
        if (localRatings.any { it.mediaId.key == rating.mediaId }) {
          continue
        }

        val entity = Rating(
          mediaId = rating.mediaId,
          type = "show",
          rating = rating.rating,
          seasonNumber = null,
          episodeNumber = null,
          ratedAt = rating.ratedAt.toUtcDateTime() ?: nowUtc(),
          createdAt = nowUtc(),
          updatedAt = nowUtc(),
        )

        localSource.ratings.replace(entity)
      }
    }
  }

  private suspend fun importSeasonsRatings(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localRatings = ratingsRepository.loadSeasonsRatings()

      for (rating in backup.ratingsSeasons) {
        if (localRatings.any { it.mediaId == rating.mediaId }) {
          continue
        }

        val entity = Rating(
          mediaId = rating.mediaId,
          type = "season",
          rating = rating.rating,
          seasonNumber = rating.seasonNumber,
          episodeNumber = null,
          ratedAt = rating.ratedAt.toUtcDateTime() ?: nowUtc(),
          createdAt = nowUtc(),
          updatedAt = nowUtc(),
        )

        localSource.ratings.replace(entity)
      }
    }
  }

  private suspend fun importEpisodesRatings(backup: BackupShows) {
    withContext(dispatchers.IO) {
      val localRatings = ratingsRepository.loadEpisodesRatings()

      for (rating in backup.ratingsEpisodes) {
        if (localRatings.any { it.mediaId == rating.mediaId }) {
          continue
        }

        val entity = Rating(
          mediaId = rating.mediaId,
          type = "episode",
          rating = rating.rating,
          seasonNumber = rating.seasonNumber,
          episodeNumber = rating.episodeNumber,
          ratedAt = rating.ratedAt.toUtcDateTime() ?: nowUtc(),
          createdAt = nowUtc(),
          updatedAt = nowUtc(),
        )

        localSource.ratings.replace(entity)
      }
    }
  }

  private suspend fun importExistingMyShowEpisodes(
    showId: MediaId,
    backup: BackupShows,
  ) {
    Timber.d("Show already in My Shows. Importing episodes ...")
    withContext(dispatchers.IO) {
      val show = localSource.shows.getById(showId.id) ?: return@withContext
      val importEpisodes = backup.progressEpisodes
        .filter { it.showTraktId == showId.id }

      val localEpisodesAsync = async { localSource.episodes.getAllByShowId(show.mediaId) }
      val localEpisodes = localEpisodesAsync.await()

      if (localEpisodes.isEmpty()) {
        return@withContext
      }

      for (importEpisode in importEpisodes) {
        val localEpisode = localEpisodes
          .firstOrNull {
            it.mediaId == importEpisode.mediaId ||
              (it.seasonNumber == importEpisode.seasonNumber && it.episodeNumber == importEpisode.episodeNumber)
          }

        if (localEpisode != null && !localEpisode.isWatched) {
          episodesManager.setEpisodeWatched(
            showId = showId,
            seasonId = localEpisode.idSeason,
            episodeId = localEpisode.mediaId,
            customDate = importEpisode.addedAt?.toUtcDateTime(),
          )
        }
      }
    }
  }

  private suspend fun loadSeasonsEpisodes(
    showId: Long,
    backupShows: BackupShows,
  ): Pair<List<Season>, List<Episode>> =
    coroutineScope {
      val remoteSeasons = remoteSource.media.fetchSeasons(showId)

      val localEpisodesAsync = async { localSource.episodes.getAllWatchedIdsForShows(listOf(showId)) }
      val localSeasonsAsync = async { localSource.seasons.getAllWatchedIdsForShows(listOf(showId)) }
      val localEpisodesIds = localEpisodesAsync.await()
      val localSeasonsIds = localSeasonsAsync.await()

      val backupSeason = backupShows.progressSeasons.filter { it.showTraktId == showId }
      val backupEpisodes = backupShows.progressEpisodes.filter { it.showTraktId == showId }

      val seasons = remoteSeasons
        .filterNot { localSeasonsIds.contains(it.ids?.media) }
        .map { mappers.season.fromNetwork(it) }
        .map { remoteSeason ->
          val isWatchedNumber = backupSeason
            .any { it.seasonNumber == remoteSeason.number }

          val isWatchedSize = backupEpisodes
            .count { it.seasonNumber == remoteSeason.number } == remoteSeason.episodes.size

          mappers.season.toDatabase(
            season = remoteSeason,
            showId = MediaId.parse(showId),
            isWatched = isWatchedNumber && isWatchedSize,
          )
        }

      val episodes = remoteSeasons.flatMap { season ->
        season.episodes
          ?.filterNot { localEpisodesIds.contains(it.ids?.media) }
          ?.map { episode ->
            val importEpisode = backupEpisodes
              .find {
                it.seasonNumber == episode.season &&
                  it.episodeNumber == episode.number
              }

            val watchedAt = importEpisode?.addedAt?.toUtcDateTime()
            val exportedAt = importEpisode?.let {
              it.addedAt?.toUtcDateTime() ?: nowUtc()
            }

            mappers.episode.toDatabase(
              showId = MediaId.parse(showId),
              season = mappers.season.fromNetwork(season),
              episode = mappers.episode.fromNetwork(episode),
              isWatched = importEpisode != null,
              lastExportedAt = exportedAt,
              lastWatchedAt = watchedAt,
            )
          } ?: emptyList()
      }

      Pair(seasons, episodes)
    }

  private suspend fun fetchShowDetails(show: BackupShow): Boolean {
    Timber.d("Fetching remote show details for ${show.mediaId} ...")
    return try {
      showsRepository.detailsShow.load(MediaId.parse(show.mediaId), force = true)
      true
    } catch (error: Throwable) {
      rethrowCancellation(error) {
        if (error is HttpException && error.code() == 404) {
          Timber.w("Failed to fetch show: ${show.mediaId} ${show.title}")
        }
      }
      false
    }
  }
}
