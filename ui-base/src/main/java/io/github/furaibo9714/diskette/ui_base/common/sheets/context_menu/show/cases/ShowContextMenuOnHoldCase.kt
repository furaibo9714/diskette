package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.OnHoldItemsRepository
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuOnHoldCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun addToOnHold(mediaId: MediaId) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))
    pinnedItemsRepository.removePinnedItem(show)
    onHoldItemsRepository.addItem(show)
    withContext(dispatchers.IO) {
      announcementManager.refreshShowsAnnouncements()
    }
  }

  suspend fun removeFromOnHold(mediaId: MediaId) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))
    onHoldItemsRepository.removeItem(show)
    withContext(dispatchers.IO) {
      announcementManager.refreshShowsAnnouncements()
    }
  }
}
