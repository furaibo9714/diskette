package io.github.furaibo9714.diskette.ui_progress.calendar.helpers

import io.github.furaibo9714.diskette.common.extensions.toZonedDateTime
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.ui_model.MediaId
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

    shows
      .filter { it.firstAired.isNotBlank() }
      .forEach { show ->
        val season = createWatchlistSeason(show)
        seasons.add(season)
        episodes.add(createWatchlistEpisode(show, season))
      }
  }

  /**
   * These rows stand in for a watchlisted show's premiere so it appears on the calendar. They are
   * never persisted, so they only need ids distinct from the real rows around them - hence a
   * [MediaId.local] scoped to the show rather than anything a provider issued.
   */
  private fun createWatchlistSeason(show: Show) =
    Season(
      mediaId = MediaId.local("${show.mediaId.key}/watchlist-season").key,
      showMediaId = show.mediaId.key,
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
  ) = Episode(
    mediaId = MediaId.local("${show.mediaId.key}/watchlist-episode").key,
    idSeason = season.mediaId,
    showMediaId = show.mediaId.key,
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
