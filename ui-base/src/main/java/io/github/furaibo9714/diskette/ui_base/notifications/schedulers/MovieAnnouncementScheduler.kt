package io.github.furaibo9714.diskette.ui_base.notifications.schedulers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.dateFromMillis
import io.github.furaibo9714.diskette.common.extensions.nowUtcDay
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.R
import io.github.furaibo9714.diskette.ui_base.fcm.NotificationChannel
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementWorker
import io.github.furaibo9714.diskette.ui_model.ImageStatus
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Translation
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MovieAnnouncementScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val moviesImagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
) {

  companion object {
    const val ANNOUNCEMENT_MOVIE_WORK_TAG = "ANNOUNCEMENT_MOVIE_WORK_TAG"
    private const val MOVIE_THRESHOLD_HOUR = 12
  }

  private val logFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }

  suspend fun scheduleAnnouncement(
    context: Context,
    movie: Movie,
    language: String,
  ) {
    var translation: Translation? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      translation = translationsRepository.loadTranslation(movie, language, onlyLocal = true)
    }

    val data = Data.Builder().apply {
      putLong(AnnouncementWorker.DATA_MOVIE_ID, movie.mediaId)
      putString(AnnouncementWorker.DATA_CHANNEL, NotificationChannel.MOVIES_ANNOUNCEMENTS.name)
      putString(AnnouncementWorker.DATA_TITLE, if (translation?.hasTitle == true) translation.title else movie.title)
      putString(AnnouncementWorker.DATA_CONTENT, context.getString(R.string.textNewMovieAvailable))

      val posterImage = moviesImagesProvider.findCachedImage(movie, ImageType.POSTER)
      if (posterImage.status == ImageStatus.AVAILABLE) {
        putString(AnnouncementWorker.DATA_IMAGE_URL, posterImage.fullFileUrl)
      } else {
        val fanartImage = moviesImagesProvider.findCachedImage(movie, ImageType.FANART)
        if (fanartImage.status == ImageStatus.AVAILABLE) {
          putString(AnnouncementWorker.DATA_IMAGE_URL, fanartImage.fullFileUrl)
        }
      }
    }

    val now = ZonedDateTime.now()
    val days = movie.released!!.toEpochDay() - nowUtcDay().toEpochDay()
    val offset = now.withHour(MOVIE_THRESHOLD_HOUR).withMinute(0).toMillis() - now.toMillis()
    val delayed = (days * TimeUnit.DAYS.toMillis(1)) + offset
    val request = OneTimeWorkRequestBuilder<AnnouncementWorker>()
      .setInputData(data.build())
      .setInitialDelay(delayed, TimeUnit.MILLISECONDS)
      .addTag(ANNOUNCEMENT_MOVIE_WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)

    val logTime = logFormatter.format(dateFromMillis(nowUtcMillis() + delayed))
    Timber.d("Notification set for ${movie.title}: $logTime UTC")
  }
}
