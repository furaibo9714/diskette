@file:Suppress("ktlint:standard:max-line-length")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource

@Dao
interface EpisodesDao : EpisodesLocalDataSource {

  @Insert(onConflict = REPLACE)
  override suspend fun upsert(episodes: List<Episode>)

  @Transaction
  override suspend fun upsertChunked(items: List<Episode>) {
    val chunks = items.chunked(500)
    chunks.forEach { chunk -> upsert(chunk) }
  }

  @Query("SELECT * FROM episodes WHERE show_media_id = :showMediaId AND media_id = :episodeMediaId")
  override suspend fun getById(
    showMediaId: String,
    episodeMediaId: String,
  ): Episode?

  @Query(
    "SELECT EXISTS(SELECT 1 FROM episodes WHERE show_media_id = :showMediaId AND media_id = :episodeMediaId AND is_watched = 1)",
  )
  override suspend fun isEpisodeWatched(
    showMediaId: String,
    episodeMediaId: String,
  ): Boolean

  @Query("SELECT * FROM episodes WHERE media_id IN(:episodesIds)")
  override suspend fun getAll(episodesIds: List<Long>): List<Episode>

  @Query("SELECT * FROM episodes WHERE id_season = :seasonMediaId")
  override suspend fun getAllForSeason(seasonMediaId: Long): List<Episode>

  @Query("SELECT * FROM episodes WHERE show_media_id = :showMediaId")
  override suspend fun getAllByShowId(showMediaId: String): List<Episode>

  @Query("SELECT * FROM episodes WHERE show_media_id = :showMediaId AND season_number = :seasonNumber")
  override suspend fun getAllByShowId(
    showMediaId: String,
    seasonNumber: Int,
  ): List<Episode>

  @Transaction
  override suspend fun getAllByShowsIds(showTraktIds: List<Long>): List<Episode> {
    val result = mutableListOf<Episode>()
    val chunks = showTraktIds.chunked(50)
    chunks.forEach { chunk ->
      result += getAllByShowsIdsChunk(chunk)
    }
    return result
  }

  @Transaction
  @Query("SELECT * FROM episodes WHERE show_media_id IN (:showTraktIds)")
  override suspend fun getAllByShowsIdsChunk(showTraktIds: List<Long>): List<Episode>

  @Transaction
  override suspend fun getAllByShowsIds(
    showTraktIds: List<Long>,
    fromTime: Long,
  ): List<Episode> {
    val result = mutableListOf<Episode>()
    val chunks = showTraktIds.chunked(50)
    chunks.forEach { chunk ->
      result += getAllByShowsIdsChunk(chunk, fromTime)
    }
    return result
  }

  @Transaction
  @Query("SELECT * FROM episodes WHERE show_media_id IN (:showTraktIds) AND first_aired >= :fromTime")
  override suspend fun getAllByShowsIdsChunk(
    showTraktIds: List<Long>,
    fromTime: Long,
  ): List<Episode>

  @Query(
    "SELECT * from episodes where show_media_id = :showMediaId AND is_watched = 0 AND season_number != 0 AND first_aired <= :toTime ORDER BY season_number ASC, episode_number ASC LIMIT 1",
  )
  override suspend fun getFirstUnwatched(
    showMediaId: String,
    toTime: Long,
  ): Episode?

  @Query(
    "SELECT * from episodes where show_media_id = :showMediaId AND is_watched = 0 AND season_number != 0 AND first_aired > :fromTime AND first_aired <= :toTime ORDER BY season_number ASC, episode_number ASC LIMIT 1",
  )
  override suspend fun getFirstUnwatched(
    showMediaId: String,
    fromTime: Long,
    toTime: Long,
  ): Episode?

  @Query(
    "SELECT * from episodes where show_media_id = :showMediaId " +
      "AND is_watched = 0 " +
      "AND season_number != 0 " +
      "AND ((season_number * 10000) + episode_number) > ((:seasonNumber * 10000) + :episodeNumber) " +
      "AND first_aired <= :toTime " +
      "ORDER BY season_number ASC, episode_number ASC LIMIT 1",
  )
  override suspend fun getFirstUnwatchedAfterEpisode(
    showMediaId: String,
    seasonNumber: Int,
    episodeNumber: Int,
    toTime: Long,
  ): Episode?

  @Query(
    "SELECT * from episodes where show_media_id = :showMediaId AND is_watched = 1 AND season_number != 0 ORDER BY last_watched_at DESC LIMIT 1",
  )
  override suspend fun getLastWatched(showMediaId: String): Episode?

  @Query(
    "SELECT COUNT(media_id) FROM episodes WHERE show_media_id = :showMediaId AND first_aired < :toTime AND season_number != 0",
  )
  override suspend fun getTotalCount(
    showMediaId: String,
    toTime: Long,
  ): Int

  @Query("SELECT COUNT(media_id) FROM episodes WHERE show_media_id = :showMediaId AND season_number != 0")
  override suspend fun getTotalCount(showMediaId: String): Int

  @Query(
    "SELECT COUNT(media_id) FROM episodes WHERE show_media_id = :showMediaId AND is_watched = 1 AND first_aired < :toTime AND season_number != 0",
  )
  override suspend fun getWatchedCount(
    showMediaId: String,
    toTime: Long,
  ): Int

  @Query(
    "SELECT COUNT(media_id) FROM episodes WHERE show_media_id = :showMediaId AND is_watched = 1 AND season_number != 0",
  )
  override suspend fun getWatchedCount(showMediaId: String): Int

  @Query("SELECT * FROM episodes WHERE is_watched = 1")
  override suspend fun getAllWatched(): List<Episode>

  @Query("SELECT * FROM episodes WHERE show_media_id IN(:showsIds) AND is_watched = 1")
  override suspend fun getAllWatchedForShows(showsIds: List<Long>): List<Episode>

  @Query(
    "SELECT * FROM episodes WHERE show_media_id IN(:showsIds) AND is_watched = 1 AND last_watched_at NOT NULL AND last_watched_at >= :fromTime AND last_watched_at <= :toTime",
  )
  override suspend fun getAllWatchedForShows(
    showsIds: List<Long>,
    fromTime: Long,
    toTime: Long,
  ): List<Episode>

  @Query("SELECT media_id FROM episodes WHERE show_media_id IN(:showsIds) AND is_watched = 1")
  override suspend fun getAllWatchedIdsForShows(showsIds: List<String>): List<String>

  @Query(
    "SELECT * FROM episodes WHERE is_watched = 1 AND last_watched_at NOT NULL AND last_watched_at >= :fromTime AND last_watched_at <= :toTime AND (show_media_id IN (SELECT media_id FROM shows_my_shows) OR show_media_id IN (SELECT media_id FROM shows_see_later)) ORDER BY last_watched_at DESC LIMIT :limit OFFSET :offset",
  )
  override suspend fun getAllWatchedForTrackedShowsPaged(
    fromTime: Long,
    toTime: Long,
    limit: Int,
    offset: Int,
  ): List<Episode>

  @Transaction
  override suspend fun updateIsExported(
    episodesIds: List<Long>,
    exportedAt: Long,
  ) {
    episodesIds.forEach {
      updateIsExported(it, exportedAt)
    }
  }

  @Query("UPDATE episodes SET last_exported_at = :exportedAt WHERE media_id = :episodeId")
  suspend fun updateIsExported(
    episodeId: Long,
    exportedAt: Long,
  )

  @Query("DELETE FROM episodes WHERE show_media_id = :showMediaId AND is_watched = 0")
  override suspend fun deleteAllUnwatchedForShow(showMediaId: String)

  @Query("DELETE FROM episodes WHERE show_media_id = :showMediaId")
  override suspend fun deleteAllForShow(showMediaId: String)

  @Delete
  override suspend fun delete(items: List<Episode>)
}
