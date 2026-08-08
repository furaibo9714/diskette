package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import io.github.furaibo9714.diskette.data_local.database.model.Episode as EpisodeDb
import io.github.furaibo9714.diskette.data_local.database.model.Season as SeasonDb

@ViewModelScoped
class ShowDetailsMyShowsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun getAllIds() =
    withContext(dispatchers.IO) {
      val (myShows, watchlistShows) = awaitAll(
        async { showsRepository.myShows.loadAllIds() },
        async { showsRepository.watchlistShows.loadAllIds() },
      )
      Pair(myShows, watchlistShows)
    }

  suspend fun isMyShows(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.myShows.exists(show.ids.media)
    }

  suspend fun addToMyShows(
    show: Show,
    seasons: List<Season>,
    episodes: List<Episode>,
  ) = withContext(dispatchers.IO) {
    transactions.withTransaction {
      val localSeasons = localSource.seasons.getAllByShowId(show.mediaId)
      val localEpisodes = localSource.episodes.getAllByShowId(show.mediaId)
      val lastWatchedAt = localEpisodes.maxByOrNull { it.lastWatchedAt != null }?.lastWatchedAt?.toMillis() ?: 0L

      showsRepository.myShows.insert(show.ids.media, lastWatchedAt)

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      seasons.forEach { season ->
        if (localSeasons.none { it.mediaId == season.ids.media.id }) {
          seasonsToAdd.add(mappers.season.toDatabase(season, show.ids.media, false))
        }
      }
      episodes.forEach { episode ->
        if (localEpisodes.none { it.mediaId == episode.ids.media.id }) {
          val season = seasons.find { it.number == episode.season }!!
          episodesToAdd.add(mappers.episode.toDatabase(episode, season, show.ids.media, false, null, null))
        }
      }

      localSource.seasons.upsert(seasonsToAdd)
      localSource.episodes.upsert(episodesToAdd)
    }

    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
  }

  suspend fun removeFromMyShows(
    show: Show,
    removeLocalData: Boolean,
  ) = withContext(dispatchers.IO) {
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
}
