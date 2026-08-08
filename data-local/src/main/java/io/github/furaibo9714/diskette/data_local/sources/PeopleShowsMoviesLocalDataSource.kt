package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.PersonShowMovie

interface PeopleShowsMoviesLocalDataSource {

  suspend fun getTimestampForShow(showMediaId: String): Long?

  suspend fun getTimestampForMovie(movieMediaId: String): Long?

  suspend fun deleteAllForShow(showMediaId: String)

  suspend fun deleteAllForMovie(movieMediaId: String)

  suspend fun insertForShow(
    people: List<PersonShowMovie>,
    showMediaId: String,
  )

  suspend fun insertForMovie(
    people: List<PersonShowMovie>,
    movieMediaId: String,
  )
}
