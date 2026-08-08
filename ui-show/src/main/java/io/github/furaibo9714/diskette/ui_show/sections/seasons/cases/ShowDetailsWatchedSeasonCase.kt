package io.github.furaibo9714.diskette.ui_show.sections.seasons.cases

import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.SeasonBundle
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchedSeasonCase @Inject constructor(
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
        val isMyShows = showsRepository.myShows.exists(show.ids.media)
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
