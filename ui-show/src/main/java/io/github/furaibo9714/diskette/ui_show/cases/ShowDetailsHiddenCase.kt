package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsHiddenCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isHidden(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.hiddenShows.exists(show.ids.trakt)
    }

  suspend fun addToHidden(
    show: Show,
    removeLocalData: Boolean,
  ) = withContext(dispatchers.IO) {
    transactions.withTransaction {
      showsRepository.hiddenShows.insert(show.ids.trakt)

      if (removeLocalData) {
        localSource.episodes.deleteAllUnwatchedForShow(show.traktId)
        val seasons = localSource.seasons.getAllByShowId(show.traktId)
        val episodes = localSource.episodes.getAllByShowId(show.traktId)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        localSource.seasons.delete(toDelete)
      }
    }
    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    floppySyncManager.scheduleShowHidden(show.ids, Operation.ADD)
  }

  suspend fun removeFromHidden(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.hiddenShows.delete(show.ids.trakt)
      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
      floppySyncManager.scheduleShowHidden(show.ids, Operation.REMOVE)
    }
}
