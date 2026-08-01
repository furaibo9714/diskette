package com.michaldrabik.ui_base.floppy

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.FloppySyncError
import com.michaldrabik.ui_base.events.FloppySyncStart
import com.michaldrabik.ui_base.events.FloppySyncSuccess
import com.michaldrabik.ui_base.floppy.imports.FloppyImportWatchedRunner
import com.michaldrabik.ui_base.floppy.imports.FloppyImportWatchlistRunner
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit.SECONDS

@SuppressLint("MissingPermission")
@HiltWorker
class FloppySyncWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val syncRunner: FloppySyncRunner,
  private val importWatchlistRunner: FloppyImportWatchlistRunner,
  private val importWatchedRunner: FloppyImportWatchedRunner,
  private val eventsManager: EventsManager,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG_ID = "FLOPPY_FULL_SYNC_WORK_ID"
    private const val TAG = "FLOPPY_SYNC_WORK"
    private const val TAG_FULL_SYNC = "FLOPPY_FULL_SYNC_WORK"
    private const val ARG_IS_FULL_SYNC = "ARG_IS_FULL_SYNC"

    private const val SYNC_NOTIFICATION_PROGRESS_ID = 921
    private const val SYNC_NOTIFICATION_SUCCESS_ID = 922
    private const val SYNC_NOTIFICATION_ERROR_ID = 923

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

    fun scheduleFullSync(workManager: WorkManager) {
      val inputData = workDataOf(ARG_IS_FULL_SYNC to true)

      val request = OneTimeWorkRequestBuilder<FloppySyncWorker>()
        .setConstraints(
          Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
        ).setInputData(inputData)
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .addTag(TAG_ID)
        .addTag(TAG_FULL_SYNC)
        .build()

      workManager.enqueueUniqueWork(TAG_FULL_SYNC, ExistingWorkPolicy.KEEP, request)
      Timber.i("Floppy full sync scheduled.")
    }
  }

  override suspend fun doWork(): Result {
    val isFullSync = inputData.getBoolean(ARG_IS_FULL_SYNC, false)

    if (isFullSync) eventsManager.sendEvent(FloppySyncStart)
    return try {
      if (isFullSync) {
        setProgressNotification(applicationContext.getString(R.string.textFloppySyncImportingWatchlist))
        val importedWatchlist = importWatchlistRunner.run()
        setProgressNotification(applicationContext.getString(R.string.textFloppySyncImportingWatched))
        val importedWatched = importWatchedRunner.run()
        Timber.d("Floppy import completed. Watchlist: $importedWatchlist, Watched: $importedWatched")
        setProgressNotification(applicationContext.getString(R.string.textFloppySyncExporting))
      }
      val count = syncRunner.run()
      Timber.d("Floppy sync completed. Pushed: $count")
      if (isFullSync) {
        eventsManager.sendEvent(FloppySyncSuccess)
        notificationManager().notify(SYNC_NOTIFICATION_SUCCESS_ID, createSuccessNotification())
      }
      Result.success()
    } catch (error: Throwable) {
      Timber.w(error, "Floppy sync failed.")
      if (isFullSync) {
        eventsManager.sendEvent(FloppySyncError)
        notificationManager().notify(SYNC_NOTIFICATION_ERROR_ID, createErrorNotification())
      }
      Result.failure()
    } finally {
      if (isFullSync) notificationManager().cancel(SYNC_NOTIFICATION_PROGRESS_ID)
    }
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    val notification = createProgressNotification(null)
    return ForegroundInfo(SYNC_NOTIFICATION_PROGRESS_ID, notification)
  }

  private fun setProgressNotification(content: String?) {
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification(content))
  }

  private fun createBaseNotification(): NotificationCompat.Builder =
    NotificationCompat
      .Builder(applicationContext, createNotificationChannel())
      .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
      .setContentTitle(applicationContext.getString(R.string.textFloppySync))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorNotificationDark))

  private fun createProgressNotification(content: String?): Notification =
    createBaseNotification()
      .setContentText(content ?: applicationContext.getString(R.string.textFloppySyncRunning))
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setOngoing(true)
      .setAutoCancel(false)
      .setProgress(0, 0, true)
      .build()

  private fun createSuccessNotification(): Notification =
    createBaseNotification()
      .setContentText(applicationContext.getString(R.string.textFloppySyncComplete))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()

  private fun createErrorNotification(): Notification =
    createBaseNotification()
      .setContentTitle(applicationContext.getString(R.string.textFloppySyncError))
      .setContentText(applicationContext.getString(R.string.textFloppySyncErrorFull))
      .setStyle(NotificationCompat.BigTextStyle().bigText(applicationContext.getString(R.string.textFloppySyncErrorFull)))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()

  private fun createNotificationChannel(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Floppy Sync Service"
      val name = "Showly Floppy Sync"
      val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      applicationContext.notificationManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }
}
