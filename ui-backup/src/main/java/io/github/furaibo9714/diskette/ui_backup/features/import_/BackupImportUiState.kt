package io.github.furaibo9714.diskette.ui_backup.features.import_

import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Idle

data class BackupImportUiState(
  val isImporting: BackupImportStatus = Idle,
  val isSuccess: Boolean = false,
  val isError: Throwable? = null,
)
