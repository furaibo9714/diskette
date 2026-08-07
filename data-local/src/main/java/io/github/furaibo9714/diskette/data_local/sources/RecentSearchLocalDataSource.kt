package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.RecentSearch

interface RecentSearchLocalDataSource {

  suspend fun getAll(limit: Int): List<RecentSearch>

  suspend fun upsert(searches: List<RecentSearch>)

  suspend fun deleteAll()
}
