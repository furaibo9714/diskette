package io.github.furaibo9714.diskette.ui_backup.di

import io.github.furaibo9714.diskette.ui_backup.features.export.workers.BackupExportWorker
import io.github.furaibo9714.diskette.ui_backup.features.export.workers.DefaultBackupExportWorker
import io.github.furaibo9714.diskette.ui_backup.features.import_.workers.BackupImportWorker
import io.github.furaibo9714.diskette.ui_backup.features.import_.workers.DefaultBackupImportWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BackupBindingModule {

  @Binds
  abstract fun bindExportWorker(worker: DefaultBackupExportWorker): BackupExportWorker

  @Binds
  abstract fun bindImportWorker(worker: DefaultBackupImportWorker): BackupImportWorker
}
