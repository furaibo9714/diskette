package io.github.furaibo9714.diskette.ui_show.episodes.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesAnnouncementsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements(idTrakt: IdTrakt) =
    withContext(dispatchers.IO) {
      val isMyShow = showsRepository.myShows.exists(idTrakt)
      if (isMyShow) {
        announcementManager.refreshShowsAnnouncements()
      }
    }
}
