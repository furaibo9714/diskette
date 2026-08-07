package io.github.furaibo9714.diskette.ui_episodes.details.cases

import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MyShowsLocalDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsSeasonCase @Inject constructor(
  private val myShowsDataSource: MyShowsLocalDataSource,
  private val episodesDataSource: EpisodesLocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadSeason(
    showId: IdTrakt,
    episode: Episode,
    seasonEpisodes: IntArray?,
  ): List<Episode> {
    val isMyShow = myShowsDataSource.checkExists(showId.id)
    if (!isMyShow) {
      return seasonEpisodes?.map {
        Episode.EMPTY.copy(season = episode.season, number = it)
      } ?: emptyList()
    }

    val episodes = episodesDataSource
      .getAllByShowId(showId.id, episode.season)
      .map { mappers.episode.fromDatabase(it) }
      .sortedBy { it.number }

    if (episodes.isNotEmpty()) return episodes
    return seasonEpisodes?.map {
      Episode.EMPTY.copy(season = episode.season, number = it)
    } ?: emptyList()
  }
}
