package io.github.furaibo9714.diskette.ui.main.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MainAnnouncementsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements() {
    withContext(dispatchers.IO) {
      announcementManager.refreshShowsAnnouncements()
      announcementManager.refreshMoviesAnnouncements()
    }
  }
}
