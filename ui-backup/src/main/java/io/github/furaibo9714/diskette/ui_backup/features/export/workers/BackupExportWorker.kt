package io.github.furaibo9714.diskette.ui_backup.features.export.workers

import io.github.furaibo9714.diskette.ui_backup.model.BackupScheme

interface BackupExportWorker {
  suspend fun run(): BackupScheme
}
