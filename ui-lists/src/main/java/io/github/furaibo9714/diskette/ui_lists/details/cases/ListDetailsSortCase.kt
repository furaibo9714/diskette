package io.github.furaibo9714.diskette.ui_lists.details.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ListDetailsSortCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun setSortOrder(
    listId: Long,
    sortOrder: SortOrder,
    sortType: SortType,
  ): CustomList =
    withContext(dispatchers.IO) {
      localSource.customLists.updateSortByLocal(
        listId,
        sortOrder.slug,
        sortType.slug,
        nowUtcMillis(),
      )
      val list = localSource.customLists.getById(listId)!!
      mappers.customList.fromDatabase(list)
    }

  suspend fun setFilterTypes(
    listId: Long,
    types: List<Mode>,
  ): CustomList =
    withContext(dispatchers.IO) {
      localSource.customLists.updateFilterTypeLocal(
        listId,
        types.joinToString(",") { it.type },
        nowUtcMillis(),
      )
      val list = localSource.customLists.getById(listId)!!
      mappers.customList.fromDatabase(list)
    }
}
