package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.PersonShowMovie
import io.github.furaibo9714.diskette.data_local.sources.PeopleShowsMoviesLocalDataSource

@Dao
interface PeopleShowsMoviesDao :
  BaseDao<PersonShowMovie>,
  PeopleShowsMoviesLocalDataSource {

  @Query("SELECT updated_at FROM people_shows_movies WHERE show_media_id == :showMediaId LIMIT 1")
  override suspend fun getTimestampForShow(showMediaId: String): Long?

  @Query("SELECT updated_at FROM people_shows_movies WHERE movie_media_id == :movieMediaId LIMIT 1")
  override suspend fun getTimestampForMovie(movieMediaId: String): Long?

  @Query("DELETE FROM people_shows_movies WHERE show_media_id == :showMediaId")
  override suspend fun deleteAllForShow(showMediaId: String)

  @Query("DELETE FROM people_shows_movies WHERE movie_media_id == :movieMediaId")
  override suspend fun deleteAllForMovie(movieMediaId: String)

  @Transaction
  override suspend fun insertForShow(
    people: List<PersonShowMovie>,
    showMediaId: String,
  ) {
    deleteAllForShow(showMediaId)
    insert(people)
  }

  @Transaction
  override suspend fun insertForMovie(
    people: List<PersonShowMovie>,
    movieMediaId: String,
  ) {
    deleteAllForMovie(movieMediaId)
    insert(people)
  }
}
