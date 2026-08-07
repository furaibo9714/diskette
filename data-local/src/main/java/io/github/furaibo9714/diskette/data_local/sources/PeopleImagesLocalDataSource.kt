package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.PersonImage

interface PeopleImagesLocalDataSource {

  suspend fun getTimestampForPerson(personTmdbId: Long): Long?

  suspend fun getAll(personTmdbId: Long): List<PersonImage>

  suspend fun deleteAllForPerson(personTmdbId: Long)

  suspend fun insertSingle(
    personTmdbId: Long,
    images: List<PersonImage>,
  )
}
