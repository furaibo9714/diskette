package io.github.furaibo9714.diskette.ui_widgets.calendar_movies

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import io.github.furaibo9714.diskette.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var calendarFutureCase: CalendarMoviesFutureCase
  @Inject lateinit var calendarRecentsCase: CalendarMoviesRecentsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?): CalendarMoviesWidgetViewsFactory {
    val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: 0
    return CalendarMoviesWidgetViewsFactory(
      widgetId,
      applicationContext,
      calendarFutureCase,
      calendarRecentsCase,
      settingsRepository,
    )
  }
}
