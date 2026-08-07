package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.PersonCredits
import io.github.furaibo9714.diskette.data_local.database.model.Show

interface PeopleCreditsLocalDataSource {

  suspend fun getAllShowsForPerson(personTraktId: Long): List<Show>

  suspend fun getAllMoviesForPerson(personTraktId: Long): List<Movie>

  suspend fun getTimestampForPerson(personTraktId: Long): Long?

  suspend fun deleteAllForPerson(personTraktId: Long)

  suspend fun insertSingle(
    personTraktId: Long,
    credits: List<PersonCredits>,
  )
}
