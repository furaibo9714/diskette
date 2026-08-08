package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Person

interface PeopleLocalDataSource {

  suspend fun upsert(people: List<Person>)

  suspend fun getById(tmdbId: Long): Person?

  suspend fun getAllForShow(showMediaId: String): List<Person>

  suspend fun getAllForMovie(movieMediaId: String): List<Person>

  suspend fun getAll(): List<Person>

  suspend fun updateMediaId(
    mediaId: String,
    idTmdb: Long,
  )

  suspend fun deleteTranslations()
}
