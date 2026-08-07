package io.github.furaibo9714.diskette.ui_backup.features.import_.model

sealed interface BackupImportStatus {
  data object Idle : BackupImportStatus

  data object Initializing : BackupImportStatus

  data class Importing(
    val title: String,
    val count: Int,
    val total: Int,
  ) : BackupImportStatus
}
