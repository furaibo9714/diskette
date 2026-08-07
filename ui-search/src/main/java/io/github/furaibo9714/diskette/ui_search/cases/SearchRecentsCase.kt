package io.github.furaibo9714.diskette.ui_search.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.ui_model.RecentSearch
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject
import io.github.furaibo9714.diskette.data_local.database.model.RecentSearch as RecentSearchDb

@ViewModelScoped
class SearchRecentsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
) {

  suspend fun getRecentSearches(limit: Int): List<RecentSearch> =
    withContext(dispatchers.IO) {
      localSource.recentSearch
        .getAll(limit)
        .map { RecentSearch(it.text) }
    }

  suspend fun clearRecentSearches() =
    withContext(dispatchers.IO) {
      localSource.recentSearch.deleteAll()
    }

  suspend fun saveRecentSearch(query: String) =
    withContext(dispatchers.IO) {
      val now = nowUtcMillis()
      localSource.recentSearch.upsert(listOf(RecentSearchDb(0, query, now, now)))
    }
}
