package io.github.furaibo9714.diskette.ui_show.sections.seasons.cases

import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.EpisodeBundle
import io.github.furaibo9714.diskette.ui_model.SeasonBundle
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_show.quicksetup.QuickSetupListItem
import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsQuickProgressCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun setQuickProgress(
    selectedItem: QuickSetupListItem,
    seasonsItems: List<SeasonListItem>,
    show: Show,
    customDate: ZonedDateTime?,
  ) = coroutineScope {
    val isMyShows = async { showsRepository.myShows.exists(show.ids.trakt) }
    val isWatchlist = async { showsRepository.watchlistShows.exists(show.ids.trakt) }
    val isHidden = async { showsRepository.hiddenShows.exists(show.ids.trakt) }

    val isCollection = isMyShows.await() || isWatchlist.await() || isHidden.await()
    val episodesAdded = mutableListOf<Episode>()

    episodesManager.setAllUnwatched(show.ids.trakt, skipSpecials = true)
    val seasons = seasonsItems.map { it.season }
    seasons
      .filter { !it.isSpecial() && it.number < selectedItem.season.number }
      .forEach { season ->
        val bundle = SeasonBundle(season, show)
        episodesManager.setSeasonWatched(bundle, customDate).apply {
          episodesAdded.addAll(this)
        }
      }

    val season = seasons.find { it.number == selectedItem.season.number }
    season
      ?.episodes
      ?.filter { it.number <= selectedItem.episode.number }
      ?.forEach { episode ->
        val bundle = EpisodeBundle(episode, season, show)
        episodesManager.setEpisodeWatched(bundle, customDate)
        episodesAdded.add(episode)
      }

    if (isCollection) {
      episodesAdded.forEach {
        floppySyncManager.scheduleEpisodeWatched(show.ids, it.season, it.number, Operation.ADD)
      }
    }
  }
}
