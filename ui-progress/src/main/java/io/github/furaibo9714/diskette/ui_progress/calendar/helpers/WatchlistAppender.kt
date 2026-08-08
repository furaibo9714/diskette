package io.github.furaibo9714.diskette.ui_progress.calendar.helpers

import io.github.furaibo9714.diskette.common.extensions.toZonedDateTime
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject

class WatchlistAppender @Inject constructor() {

  fun appendWatchlistShows(
    shows: List<Show>,
    seasons: MutableList<Season>,
    episodes: MutableList<Episode>,
  ) {
    if (shows.isEmpty() || seasons.isEmpty() || episodes.isEmpty()) {
      return
    }

    val seasonId = seasons.maxOf { it.mediaId }
    val episodeId = episodes.maxOf { it.mediaId }

    shows
      .filter { it.firstAired.isNotBlank() }
      .forEachIndexed { index, show ->
        val season = createWatchlistSeason(
          show = show,
          seasonId = seasonId + index + 1,
        )

        val episode = createWatchlistEpisode(
          show = show,
          season = season,
          episodeId = episodeId + index + 1,
        )

        seasons.add(season)
        episodes.add(episode)
      }
  }

  private fun createWatchlistSeason(
    show: Show,
    seasonId: Long,
  ) = Season(
    mediaId = seasonId,
    showMediaId = show.mediaId,
    seasonNumber = 1,
    seasonTitle = "",
    seasonOverview = "",
    seasonFirstAired = show.firstAired.toZonedDateTime(),
    episodesCount = 1,
    episodesAiredCount = 0,
    rating = null,
    isWatched = false,
  )

  private fun createWatchlistEpisode(
    show: Show,
    season: Season,
    episodeId: Long,
  ) = Episode(
    mediaId = episodeId,
    idSeason = season.mediaId,
    showMediaId = show.mediaId,
    idShowTvdb = show.ids.tvdb.id,
    idShowImdb = show.ids.imdb.id,
    idShowTmdb = show.ids.tmdb.id,
    seasonNumber = 1,
    episodeNumber = 1,
    episodeNumberAbs = null,
    episodeOverview = "",
    title = "",
    firstAired = show.firstAired.toZonedDateTime(),
    commentsCount = 0,
    rating = 0.0f,
    runtime = 0,
    votesCount = 0,
    isWatched = false,
    lastExportedAt = null,
    lastWatchedAt = null,
  )
}
