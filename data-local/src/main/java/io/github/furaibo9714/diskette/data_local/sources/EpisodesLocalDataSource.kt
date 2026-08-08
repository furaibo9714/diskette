@file:Suppress("ktlint:standard:max-line-length")

package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Episode

interface EpisodesLocalDataSource {

  suspend fun upsert(episodes: List<Episode>)

  suspend fun upsertChunked(items: List<Episode>)

  suspend fun updateIsExported(
    episodesIds: List<String>,
    exportedAt: Long,
  )

  suspend fun isEpisodeWatched(
    showMediaId: String,
    episodeMediaId: String,
  ): Boolean

  suspend fun getById(
    showMediaId: String,
    episodeMediaId: String,
  ): Episode?

  suspend fun getAll(episodesIds: List<String>): List<Episode>

  suspend fun getAllForSeason(seasonMediaId: String): List<Episode>

  suspend fun getAllByShowId(showMediaId: String): List<Episode>

  suspend fun getAllByShowId(
    showMediaId: String,
    seasonNumber: Int,
  ): List<Episode>

  suspend fun getAllByShowsIds(showMediaIds: List<String>): List<Episode>

  suspend fun getAllByShowsIds(
    showMediaIds: List<String>,
    fromTime: Long,
  ): List<Episode>

  suspend fun getAllByShowsIdsChunk(showMediaIds: List<String>): List<Episode>

  suspend fun getAllByShowsIdsChunk(
    showMediaIds: List<String>,
    fromTime: Long,
  ): List<Episode>

  suspend fun getFirstUnwatched(
    showMediaId: String,
    toTime: Long,
  ): Episode?

  suspend fun getFirstUnwatched(
    showMediaId: String,
    fromTime: Long,
    toTime: Long,
  ): Episode?

  suspend fun getFirstUnwatchedAfterEpisode(
    showMediaId: String,
    seasonNumber: Int,
    episodeNumber: Int,
    toTime: Long,
  ): Episode?

  suspend fun getLastWatched(showMediaId: String): Episode?

  suspend fun getTotalCount(
    showMediaId: String,
    toTime: Long,
  ): Int

  suspend fun getTotalCount(showMediaId: String): Int

  suspend fun getWatchedCount(
    showMediaId: String,
    toTime: Long,
  ): Int

  suspend fun getWatchedCount(showMediaId: String): Int

  suspend fun getAllWatched(): List<Episode>

  suspend fun getAllWatchedForShows(showsIds: List<String>): List<Episode>

  suspend fun getAllWatchedForShows(
    showsIds: List<String>,
    fromTime: Long,
    toTime: Long,
  ): List<Episode>

  suspend fun getAllWatchedIdsForShows(showsIds: List<String>): List<String>

  suspend fun getAllWatchedForTrackedShowsPaged(
    fromTime: Long,
    toTime: Long,
    limit: Int,
    offset: Int,
  ): List<Episode>

  suspend fun deleteAllUnwatchedForShow(showMediaId: String)

  suspend fun deleteAllForShow(showMediaId: String)

  suspend fun delete(items: List<Episode>)
}
