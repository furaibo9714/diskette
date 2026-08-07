package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.DiscoverMovie

interface DiscoverMoviesLocalDataSource {

  suspend fun getAll(): List<DiscoverMovie>

  suspend fun getMostRecent(): DiscoverMovie?

  suspend fun upsert(movies: List<DiscoverMovie>)

  suspend fun deleteAll()

  suspend fun replace(movies: List<DiscoverMovie>)
}
