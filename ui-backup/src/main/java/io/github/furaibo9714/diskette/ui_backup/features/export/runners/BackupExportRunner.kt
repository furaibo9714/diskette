package io.github.furaibo9714.diskette.ui_backup.features.export.runners

internal abstract class BackupExportRunner<T> {
  abstract suspend fun run(): T
}
