package io.github.furaibo9714.diskette.ui_backup.features.import_.workers

import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus
import io.github.furaibo9714.diskette.ui_backup.model.BackupScheme

interface BackupImportWorker {
  suspend fun run(backup: BackupScheme)

  var statusListener: ((BackupImportStatus) -> Unit)?
}
