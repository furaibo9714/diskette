package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.PersonCredits
import io.github.furaibo9714.diskette.data_local.database.model.Show

interface PeopleCreditsLocalDataSource {

  suspend fun getAllShowsForPerson(personMediaId: String): List<Show>

  suspend fun getAllMoviesForPerson(personMediaId: String): List<Movie>

  suspend fun getTimestampForPerson(personMediaId: String): Long?

  suspend fun deleteAllForPerson(personMediaId: String)

  suspend fun insertSingle(
    personMediaId: String,
    credits: List<PersonCredits>,
  )
}
