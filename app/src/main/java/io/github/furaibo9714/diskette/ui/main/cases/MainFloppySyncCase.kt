package io.github.furaibo9714.diskette.ui.main.cases

import androidx.work.WorkManager
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncWorker
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
