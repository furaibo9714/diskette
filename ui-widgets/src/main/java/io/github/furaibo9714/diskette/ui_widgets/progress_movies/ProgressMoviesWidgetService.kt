package io.github.furaibo9714.diskette.ui_widgets.progress_movies

import android.content.Intent
import android.widget.RemoteViewsService
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_progress_movies.progress.cases.ProgressMoviesItemsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProgressMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressMoviesItemsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    ProgressMoviesWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      settingsRepository,
    )
}
