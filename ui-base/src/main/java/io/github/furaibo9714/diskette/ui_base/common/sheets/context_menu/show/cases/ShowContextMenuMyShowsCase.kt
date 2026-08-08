package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.Episode as EpisodeDb
import io.github.furaibo9714.diskette.data_local.database.model.Season as SeasonDb
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class ShowContextMenuMyShowsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToMyShows(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))

      val seasons = remoteSource.media
        .fetchSeasons(mediaId.id)
        .map { mappers.season.fromNetwork(it) }
        .filter { it.episodes.isNotEmpty() }
        .filter { if (!showSpecials()) !it.isSpecial() else true }

      val episodes = seasons.flatMap { it.episodes }

      transactions.withTransaction {
        val localSeasons = localSource.seasons.getAllByShowId(mediaId.id)
        val localEpisodes = localSource.episodes.getAllByShowId(mediaId.id)
        val lastWatchedAt = localEpisodes.maxByOrNull { it.lastWatchedAt != null }?.lastWatchedAt?.toMillis() ?: 0L

        showsRepository.myShows.insert(mediaId, lastWatchedAt)

        val seasonsToAdd = mutableListOf<SeasonDb>()
        val episodesToAdd = mutableListOf<EpisodeDb>()

        seasons.forEach { season ->
          if (localSeasons.none { it.mediaId == season.ids.media.id }) {
            seasonsToAdd.add(mappers.season.toDatabase(season, mediaId, false))
          }
        }
        episodes.forEach { episode ->
          if (localEpisodes.none { it.mediaId == episode.ids.media.id }) {
            val season = seasons.find { it.number == episode.season }!!
            episodesToAdd.add(mappers.episode.toDatabase(episode, season, mediaId, false, null, null))
          }
        }

        localSource.seasons.upsert(seasonsToAdd)
        localSource.episodes.upsert(episodesToAdd)
      }

      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
    }

  suspend fun removeFromMyShows(
    mediaId: MediaId,
    removeLocalData: Boolean,
  ) = withContext(dispatchers.IO) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))
    transactions.withTransaction {
      showsRepository.myShows.delete(show.ids.media)

      if (removeLocalData) {
        localSource.episodes.deleteAllUnwatchedForShow(show.mediaId)
        val seasons = localSource.seasons.getAllByShowId(show.mediaId)
        val episodes = localSource.episodes.getAllByShowId(show.mediaId)
        val toDelete = mutableListOf<SeasonDb>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.mediaId }) {
            toDelete.add(season)
          }
        }
        localSource.seasons.delete(toDelete)
      }

      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
    }
  }

  private suspend fun showSpecials() =
    withContext(dispatchers.IO) {
      settingsRepository.load().specialSeasonsEnabled
    }
}
