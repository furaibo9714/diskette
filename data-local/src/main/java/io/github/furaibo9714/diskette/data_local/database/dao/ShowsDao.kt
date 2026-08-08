package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.database.model.ShowSearch
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource

@Dao
interface ShowsDao :
  BaseDao<Show>,
  ShowsLocalDataSource {

  @Query("SELECT * FROM shows")
  override suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows WHERE media_id IN (:ids)")
  override suspend fun getAll(ids: List<String>): List<Show>

  @Query("SELECT media_id, id_tmdb FROM shows WHERE media_id IN (:mediaIds)")
  override suspend fun getAllTmdbIds(
    mediaIds: List<String>,
  ): Map<@MapColumn(columnName = "media_id") String, @MapColumn(columnName = "id_tmdb") Long>

  @Query("SELECT shows.media_id, shows.title FROM shows")
  override suspend fun getAllForSearch(): List<ShowSearch>

  @Transaction
  override suspend fun getAllChunked(ids: List<String>): List<Show> =
    ids
      .chunked(500)
      .fold(mutableListOf()) { acc, chunk ->
        acc += getAll(chunk)
        acc
      }

  @Query("SELECT * FROM shows WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): Show?

  @Query("SELECT * FROM shows WHERE id_tmdb == :tmdbId")
  override suspend fun getByTmdbId(tmdbId: Long): Show?

  @Query("SELECT * FROM shows WHERE id_slug == :slug")
  override suspend fun getBySlug(slug: String): Show?

  @Query("SELECT * FROM shows WHERE id_imdb == :imdbId")
  override suspend fun getByImdbId(imdbId: String): Show?

  @Query("DELETE FROM shows where media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)

  @Transaction
  override suspend fun upsert(shows: List<Show>) {
    val result = insert(shows)

    val updateList = mutableListOf<Show>()
    result.forEachIndexed { index, id ->
      if (id == -1L) updateList.add(shows[index])
    }

    if (updateList.isNotEmpty()) update(updateList)
  }
}
