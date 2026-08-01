package com.michaldrabik.ui_base.floppy

/**
 * Floppy addresses watched episodes by (show, season_number, episode_number), not by any
 * TMDB-internal per-episode id - unlike shows/movies, TMDB's own per-episode `id` field (which
 * the normal TMDB-fetch path keys episode identity off, see [TmdbSyntheticIds] and
 * `TmdbToTraktModelConverter.toEpisode`) can't be derived from season/episode numbers alone, so a
 * thin episode row created from a Floppy import can't predict the id a later real fetch would
 * assign to the same episode.
 *
 * That's fine: `EpisodesManager.invalidateSeasons` (run whenever a followed show's seasons are
 * refreshed from network) already reconciles local episodes against freshly-fetched ones by
 * (season_number, episode_number) first, id only as a fallback - so any deterministic,
 * table-scoped-unique id works here. The real fetch's row replaces this thin one on the show's
 * next season refresh, carrying the watched flag over via that number-based match.
 */
object FloppyEpisodeSyntheticIds {

  private const val SHOW_MULTIPLIER = 1_000_000_000L
  private const val SEASON_MULTIPLIER = 100_000L

  fun toSeasonTraktId(
    showTraktId: Long,
    seasonNumber: Int,
  ): Long = showTraktId * SHOW_MULTIPLIER + seasonNumber * SEASON_MULTIPLIER

  fun toEpisodeTraktId(
    showTraktId: Long,
    seasonNumber: Int,
    episodeNumber: Int,
  ): Long = toSeasonTraktId(showTraktId, seasonNumber) + episodeNumber
}
