package io.github.furaibo9714.diskette.ui_statistics

import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import io.github.furaibo9714.diskette.ui_statistics.views.ratings.recycler.StatisticsRatingItem

data class StatisticsUiState(
  val mostWatchedShows: List<StatisticsMostWatchedItem>? = null,
  val mostWatchedTotalCount: Int? = null,
  val totalTimeSpentMinutes: Int? = null,
  val totalWatchedEpisodes: Int? = null,
  val totalWatchedEpisodesShows: Int? = null,
  val topGenres: List<Genre>? = null,
  val ratings: List<StatisticsRatingItem>? = null,
)
