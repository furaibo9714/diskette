package io.github.furaibo9714.diskette.ui_lists.details.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ListDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val listsRepository: ListsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun loadDetails(id: Long) =
    withContext(dispatchers.IO) {
      listsRepository.loadById(id)
    }

  suspend fun updateRanks(
    listId: Long,
    items: List<ListDetailsItem>,
  ): List<ListDetailsItem> =
    withContext(dispatchers.IO) {
      val now = nowUtcMillis()
      val listItems = listsRepository.loadItemsById(listId)
      val updateItems = mutableListOf<ListDetailsItem>()
      val updateItemsDb = mutableListOf<CustomListItem>()
      items.forEachIndexed { index, item ->
        val dbItem = listItems.first { it.id == item.id }.copy(rank = index + 1L, updatedAt = now)
        val updatedItem = item.copy(rank = index + 1L)
        updateItems.add(updatedItem)
        updateItemsDb.add(dbItem)
      }
      transactions.withTransaction {
        localSource.customListsItems.update(updateItemsDb)
        localSource.customLists.updateTimestamp(listId, now)
      }
      updateItems
    }

  suspend fun deleteList(listId: Long) =
    withContext(dispatchers.IO) {
      val list = listsRepository.loadById(listId)
      floppySyncManager.scheduleListDelete(list.idFloppy)
      listsRepository.deleteList(listId)
    }
}
