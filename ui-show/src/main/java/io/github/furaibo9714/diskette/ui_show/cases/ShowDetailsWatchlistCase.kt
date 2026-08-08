package io.github.furaibo9714.diskette.ui_show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchlistCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun isWatchlist(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.watchlistShows.exists(show.ids.media)
    }

  suspend fun addToWatchlist(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.watchlistShows.insert(show.ids.media)
      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
      floppySyncManager.scheduleShowWatchlist(show.ids, Operation.ADD)
    }

  suspend fun removeFromWatchlist(show: Show) =
    withContext(dispatchers.IO) {
      showsRepository.watchlistShows.delete(show.ids.media)
      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
      floppySyncManager.scheduleShowWatchlist(show.ids, Operation.REMOVE)
    }
}
