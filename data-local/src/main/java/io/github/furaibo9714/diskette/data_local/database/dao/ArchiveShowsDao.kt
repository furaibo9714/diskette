@file:Suppress("ktlint:standard:max-line-length")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.sources.ArchiveShowsLocalDataSource

@Dao
interface ArchiveShowsDao : ArchiveShowsLocalDataSource {

  @Query(
    "SELECT " +
      "shows.media_id, " +
      "shows.id_tvdb, " +
      "shows.id_tmdb, " +
      "shows.id_imdb, " +
      "shows.id_slug, " +
      "shows.id_tvrage, " +
      "shows.title, " +
      "shows.year, " +
      "shows.overview, " +
      "shows.first_aired, " +
      "shows.runtime, " +
      "shows.airtime_day, " +
      "shows.airtime_time, " +
      "shows.airtime_timezone, " +
      "shows.certification, " +
      "shows.network, " +
      "shows.country, " +
      "shows.trailer, " +
      "shows.homepage, " +
      "shows.status, " +
      "shows.rating, " +
      "shows.votes, " +
      "shows.comment_count, " +
      "shows.genres, " +
      "shows.aired_episodes, " +
      "shows.runtime_max, " +
      "shows_archive.updated_at, " +
      "shows_archive.created_at " +
      "FROM shows " +
      "INNER JOIN shows_archive USING(media_id)",
  )
  override suspend fun getAll(): List<Show>

  @Query(
    "SELECT " +
      "shows.media_id, " +
      "shows.id_tvdb, " +
      "shows.id_tmdb, " +
      "shows.id_imdb, " +
      "shows.id_slug, " +
      "shows.id_tvrage, " +
      "shows.title, " +
      "shows.year, " +
      "shows.overview, " +
      "shows.first_aired, " +
      "shows.runtime, " +
      "shows.airtime_day, " +
      "shows.airtime_time, " +
      "shows.airtime_timezone, " +
      "shows.certification, " +
      "shows.network, " +
      "shows.country, " +
      "shows.trailer, " +
      "shows.homepage, " +
      "shows.status, " +
      "shows.rating, " +
      "shows.votes, " +
      "shows.comment_count, " +
      "shows.genres, " +
      "shows.aired_episodes, " +
      "shows.runtime_max, " +
      "shows_archive.updated_at, " +
      "shows_archive.created_at " +
      "FROM shows " +
      "INNER JOIN shows_archive USING(media_id) WHERE media_id IN (:ids)",
  )
  override suspend fun getAll(ids: List<String>): List<Show>

  @Query("SELECT shows.media_id FROM shows INNER JOIN shows_archive USING(media_id)")
  override suspend fun getAllMediaIds(): List<String>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_archive USING(media_id) WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(show: ArchiveShow)

  @Query("DELETE FROM shows_archive WHERE media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)
}
