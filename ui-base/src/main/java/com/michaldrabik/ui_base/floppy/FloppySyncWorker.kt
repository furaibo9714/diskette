package com.michaldrabik.ui_base.floppy

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit.SECONDS

@HiltWorker
class FloppySyncWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val syncRunner: FloppySyncRunner,
) : CoroutineWorker(context, workerParams) {

  companion object {
    private const val TAG = "FLOPPY_SYNC_WORK"

    fun schedule(workManager: WorkManager) {
      val request = OneTimeWorkRequestBuilder<FloppySyncWorker>()
        .setConstraints(
          Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
        ).setInitialDelay(3, SECONDS)
        .addTag(TAG)
        .build()

      workManager.enqueueUniqueWork(TAG, APPEND_OR_REPLACE, request)
      Timber.i("Floppy sync scheduled.")
    }
  }

  override suspend fun doWork(): Result =
    try {
      val count = syncRunner.run()
      Timber.d("Floppy sync completed. Pushed: $count")
      Result.success()
    } catch (error: Throwable) {
      Timber.w(error, "Floppy sync failed.")
      Result.failure()
    }
}
