package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MyMovie
import io.github.furaibo9714.diskette.data_local.sources.MyMoviesLocalDataSource

@Dao
interface MyMoviesDao : MyMoviesLocalDataSource {

  @Query(
    "SELECT " +
      "movies.media_id, " +
      "movies.id_tmdb, " +
      "movies.id_imdb, " +
      "movies.id_slug, " +
      "movies.title, " +
      "movies.year, " +
      "movies.overview, " +
      "movies.released, " +
      "movies.runtime, " +
      "movies.country, " +
      "movies.trailer, " +
      "movies.language, " +
      "movies.homepage, " +
      "movies.status, " +
      "movies.rating, " +
      "movies.votes, " +
      "movies.comment_count, " +
      "movies.genres, " +
      "movies_my_movies.updated_at, " +
      "movies_my_movies.created_at " +
      "FROM movies " +
      "INNER JOIN movies_my_movies USING(media_id)",
  )
  override suspend fun getAll(): List<Movie>

  @Query(
    "SELECT " +
      "movies.media_id, " +
      "movies.id_tmdb, " +
      "movies.id_imdb, " +
      "movies.id_slug, " +
      "movies.title, " +
      "movies.year, " +
      "movies.overview, " +
      "movies.released, " +
      "movies.runtime, " +
      "movies.country, " +
      "movies.trailer, " +
      "movies.language, " +
      "movies.homepage, " +
      "movies.status, " +
      "movies.rating, " +
      "movies.votes, " +
      "movies.comment_count, " +
      "movies.genres, " +
      "movies.created_at, " +
      "movies_my_movies.updated_at " +
      "FROM movies " +
      "INNER JOIN movies_my_movies USING(media_id) WHERE media_id IN (:ids)",
  )
  override suspend fun getAll(ids: List<String>): List<Movie>

  @Query(
    "SELECT movies.* FROM movies " +
      "INNER JOIN movies_my_movies USING(media_id) ORDER BY movies_my_movies.updated_at DESC LIMIT :limit",
  )
  override suspend fun getAllRecent(limit: Int): List<Movie>

  @Query("SELECT movies.media_id FROM movies INNER JOIN movies_my_movies USING(media_id)")
  override suspend fun getAllMediaIds(): List<String>

  @Query(
    "SELECT " +
      "movies.media_id, " +
      "movies.id_tmdb, " +
      "movies.id_imdb, " +
      "movies.id_slug, " +
      "movies.title, " +
      "movies.year, " +
      "movies.overview, " +
      "movies.released, " +
      "movies.runtime, " +
      "movies.country, " +
      "movies.trailer, " +
      "movies.language, " +
      "movies.homepage, " +
      "movies.status, " +
      "movies.rating, " +
      "movies.votes, " +
      "movies.comment_count, " +
      "movies.genres, " +
      "movies.created_at, " +
      "movies_my_movies.updated_at " +
      "FROM movies " +
      "INNER JOIN movies_my_movies USING(media_id) WHERE media_id == :mediaId",
  )
  override suspend fun getById(mediaId: String): Movie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(movies: List<MyMovie>)

  @Query("DELETE FROM movies_my_movies WHERE media_id == :mediaId")
  override suspend fun deleteById(mediaId: String)

  @Query("SELECT EXISTS(SELECT 1 FROM movies_my_movies WHERE media_id = :mediaId LIMIT 1);")
  override suspend fun checkExists(mediaId: String): Boolean
}
