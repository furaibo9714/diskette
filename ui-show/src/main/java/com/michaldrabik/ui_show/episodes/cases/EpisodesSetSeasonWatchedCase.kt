package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.floppy.FloppySyncManager
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class EpisodesSetSeasonWatchedCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun setSeasonWatched(
    show: Show,
    season: Season,
    isChecked: Boolean,
    customDate: ZonedDateTime?,
  ) {
    val bundle = SeasonBundle(season, show)

    when {
      isChecked -> {
        val isMyShows = showsRepository.myShows.exists(show.ids.trakt)
        val episodesAdded = episodesManager.setSeasonWatched(bundle, customDate)
        if (isMyShows) {
          episodesAdded.forEach {
            floppySyncManager.scheduleEpisodeWatched(show.ids, it.season, it.number, Operation.ADD)
          }
        }
      }
      else -> {
        episodesManager.setSeasonUnwatched(bundle)
        season.episodes.forEach {
          floppySyncManager.scheduleEpisodeWatched(show.ids, it.season, it.number, Operation.REMOVE)
        }
      }
    }
  }
}
