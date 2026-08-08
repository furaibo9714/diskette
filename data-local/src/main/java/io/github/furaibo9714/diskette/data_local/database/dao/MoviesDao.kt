package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MovieSearch
import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource

@Dao
interface MoviesDao :
  BaseDao<Movie>,
  MoviesLocalDataSource {

  @Query("SELECT * FROM movies")
  override suspend fun getAll(): List<Movie>

  @Query("SELECT * FROM movies WHERE media_id IN (:ids)")
  override suspend fun getAll(ids: List<String>): List<Movie>

  @Query("SELECT media_id, id_tmdb FROM movies WHERE media_id IN (:mediaIds)")
  override suspend fun getAllTmdbIds(
    mediaIds: List<String>,
  ): Map<@MapColumn(columnName = "media_id") String, @MapColumn(columnName = "id_tmdb") Long>

  @Query("SELECT movies.media_id, movies.title FROM movies")
  override suspend fun getAllForSearch(): List<MovieSearch>

  @Transaction
  override suspend fun getAllChunked(ids: List<String>): List<Movie> =
    ids
      .chunked(500)
      .fold(
        mutableListOf(),
      ) { acc, chunk ->
        acc += getAll(chunk)
        acc
      }

  @Query("SELECT * FROM movies WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): Movie?

  @Query("SELECT * FROM movies WHERE id_tmdb == :tmdbId")
  override suspend fun getByTmdbId(tmdbId: Long): Movie?

  @Query("SELECT * FROM movies WHERE id_slug == :slug")
  override suspend fun getBySlug(slug: String): Movie?

  @Query("SELECT * FROM movies WHERE id_imdb == :imdbId")
  override suspend fun getByImdbId(imdbId: String): Movie?

  @Query("DELETE FROM movies where media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)

  @Transaction
  override suspend fun upsert(movies: List<Movie>) {
    val result = insert(movies)

    val updateList = mutableListOf<Movie>()
    result.forEachIndexed { index, id ->
      if (id == -1L) {
        updateList.add(movies[index])
      }
    }
    if (updateList.isNotEmpty()) {
      update(updateList)
    }
  }
}
