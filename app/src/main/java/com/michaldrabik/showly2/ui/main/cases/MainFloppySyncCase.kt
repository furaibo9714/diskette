package com.michaldrabik.showly2.ui.main.cases

import androidx.work.WorkManager
import com.michaldrabik.ui_base.floppy.FloppySyncWorker
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainFloppySyncCase @Inject constructor(
  private val workManager: WorkManager,
) {

  fun refreshFloppySync() {
    FloppySyncWorker.scheduleFullSync(workManager)
  }
}
