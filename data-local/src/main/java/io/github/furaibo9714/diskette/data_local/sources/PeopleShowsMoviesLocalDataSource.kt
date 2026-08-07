package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.PersonShowMovie

interface PeopleShowsMoviesLocalDataSource {

  suspend fun getTimestampForShow(showTraktId: Long): Long?

  suspend fun getTimestampForMovie(movieTraktId: Long): Long?

  suspend fun deleteAllForShow(showTraktId: Long)

  suspend fun deleteAllForMovie(movieTraktId: Long)

  suspend fun insertForShow(
    people: List<PersonShowMovie>,
    showTraktId: Long,
  )

  suspend fun insertForMovie(
    people: List<PersonShowMovie>,
    movieTraktId: Long,
  )
}
