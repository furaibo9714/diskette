package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuWatchlistCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToWatchlist(
    mediaId: MediaId,
    removeLocalData: Boolean,
  ) = withContext(dispatchers.IO) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))

    val isMyShow = showsRepository.myShows.exists(mediaId)

    transactions.withTransaction {
      showsRepository.watchlistShows.insert(show.ids.media)

      if (removeLocalData && isMyShow) {
        localSource.episodes.deleteAllUnwatchedForShow(mediaId.key)
        val seasons = localSource.seasons.getAllByShowId(mediaId.key)
        val episodes = localSource.episodes.getAllByShowId(mediaId.key)
        val toDelete = mutableListOf<Season>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.mediaId }) {
            toDelete.add(season)
          }
        }
        localSource.seasons.delete(toDelete)
      }
    }

    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
    floppySyncManager.scheduleShowWatchlist(mediaId, Operation.ADD)
  }

  suspend fun removeFromWatchlist(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      showsRepository.watchlistShows.delete(mediaId)
      announcementManager.refreshShowsAnnouncements()
      floppySyncManager.scheduleShowWatchlist(mediaId, Operation.REMOVE)
    }
}
