package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.data_local.database.model.FloppySyncQueue.Operation
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.floppy.FloppySyncManager
import com.michaldrabik.ui_model.EpisodeBundle
import dagger.hilt.android.scopes.ViewModelScoped
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class EpisodesSetEpisodeWatchedCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun setEpisodeWatched(
    episodeBundle: EpisodeBundle,
    isChecked: Boolean,
    customDate: ZonedDateTime?,
  ) {
    val (episode, _, show) = episodeBundle

    when {
      isChecked -> {
        val isMyShows = showsRepository.myShows.exists(show.ids.trakt)
        episodesManager.setEpisodeWatched(episodeBundle, customDate)
        if (isMyShows) {
          floppySyncManager.scheduleEpisodeWatched(show.ids, episode.season, episode.number, Operation.ADD)
        }
      }
      else -> {
        episodesManager.setEpisodeUnwatched(episodeBundle)
        floppySyncManager.scheduleEpisodeWatched(show.ids, episode.season, episode.number, Operation.REMOVE)
      }
    }
  }
}
