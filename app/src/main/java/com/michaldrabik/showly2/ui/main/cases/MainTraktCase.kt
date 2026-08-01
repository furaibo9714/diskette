package com.michaldrabik.showly2.ui.main.cases

import androidx.work.WorkManager
import com.michaldrabik.ui_base.floppy.FloppySyncWorker
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncWorker
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainTraktCase @Inject constructor(
  private val quickSyncManager: QuickSyncManager,
  private val workManager: WorkManager,
) {

  fun refreshFloppySync() {
    FloppySyncWorker.scheduleFullSync(workManager)
  }

  suspend fun refreshTraktQuickSync() {
    if (quickSyncManager.isAnyScheduled()) {
      QuickSyncWorker.schedule(workManager)
    }
  }
}
