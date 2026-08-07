package io.github.furaibo9714.diskette.ui_settings.sections.notifications.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.NotificationDelay
import io.github.furaibo9714.diskette.ui_model.Settings
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsNotificationsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun getSettings(): Settings =
    withContext(dispatchers.IO) {
      settingsRepository.load()
    }

  suspend fun enableNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsEnabled = enable)
      settingsRepository.update(new)

      announcementManager.refreshShowsAnnouncements()
      announcementManager.refreshMoviesAnnouncements()
    }
  }

  suspend fun setWhenToNotify(delay: NotificationDelay) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsDelay = delay)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements()
    }
  }
}
