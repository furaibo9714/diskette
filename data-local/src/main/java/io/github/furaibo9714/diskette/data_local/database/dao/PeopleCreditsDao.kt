@file:Suppress("ktlint")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.PersonCredits
import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.sources.PeopleCreditsLocalDataSource

@Dao
interface PeopleCreditsDao : BaseDao<PersonCredits>, PeopleCreditsLocalDataSource {

  @Query(
    """
    SELECT
    shows.media_id,
    shows.id_tvdb,
    shows.id_tmdb,
    shows.id_imdb,
    shows.id_slug,
    shows.title,
    shows.year,
    shows.overview,
    shows.first_aired,
    shows.runtime,
    shows.airtime_day,
    shows.airtime_time,
    shows.airtime_timezone,
    shows.certification,
    shows.network,
    shows.country,
    shows.trailer,
    shows.homepage,
    shows.status,
    shows.rating,
    shows.votes,
    shows.comment_count,
    shows.genres,
    shows.aired_episodes,
    shows.runtime_max,
    people_credits.created_at AS created_at,
    people_credits.updated_at AS updated_at
    FROM shows
    INNER JOIN people_credits ON people_credits.show_media_id = shows.media_id
    WHERE people_credits.person_tmdb_id = :personTmdbId
    """
  )
  override suspend fun getAllShowsForPerson(personTmdbId: Long): List<Show>

  @Query(
    """
    SELECT
    movies.media_id,
    movies.id_tmdb,
    movies.id_imdb,
    movies.id_slug,
    movies.title,
    movies.year,
    movies.overview,
    movies.released,
    movies.runtime,
    movies.country,
    movies.trailer,
    movies.language,
    movies.homepage,
    movies.status,
    movies.rating,
    movies.votes,
    movies.comment_count,
    movies.genres,
    people_credits.updated_at AS updated_at,
    people_credits.created_at AS created_at
    FROM movies
    INNER JOIN people_credits ON people_credits.movie_media_id = movies.media_id
    WHERE people_credits.person_tmdb_id = :personTmdbId
    """
  )
  override suspend fun getAllMoviesForPerson(personTmdbId: Long): List<Movie>

  @Query("SELECT updated_at FROM people_credits WHERE person_tmdb_id = :personTmdbId LIMIT 1")
  override suspend fun getTimestampForPerson(personTmdbId: Long): Long?

  @Query("DELETE FROM people_credits WHERE person_tmdb_id == :personTmdbId")
  override suspend fun deleteAllForPerson(personTmdbId: Long)

  @Transaction
  override suspend fun insertSingle(
    personTmdbId: Long,
    credits: List<PersonCredits>
  ) {
    deleteAllForPerson(personTmdbId)
    insert(credits)
  }
}
