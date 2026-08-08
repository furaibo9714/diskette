package io.github.furaibo9714.diskette.ui_lists.manage.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_lists.manage.recycler.ManageListsItem
import io.github.furaibo9714.diskette.ui_model.MediaId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ManageListsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val listsRepository: ListsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun loadLists(
    itemId: MediaId,
    itemType: String,
  ) = withContext(dispatchers.IO) {
    val listsAsync = async { listsRepository.loadAll() }
    val listsWithItemAsync = async { listsRepository.loadListIdsForItem(itemId, itemType) }
    val (lists, listsWithItem) = Pair(listsAsync.await(), listsWithItemAsync.await())
    lists
      .sortedBy { it.name }
      .map {
        val isChecked = listsWithItem.contains(it.id)
        ManageListsItem(it, isChecked, true)
      }
  }

  suspend fun addToList(
    itemId: MediaId,
    itemType: String,
    listItem: ManageListsItem,
  ) = withContext(dispatchers.IO) {
    listsRepository.addToList(listItem.list.id, itemId, itemType)
    floppySyncManager.scheduleListItemAdd(itemId, Mode.fromType(itemType), listItem.list.idFloppy)
  }

  suspend fun removeFromList(
    itemId: MediaId,
    itemType: String,
    listItem: ManageListsItem,
  ) = withContext(dispatchers.IO) {
    listsRepository.removeFromList(listItem.list.id, itemId, itemType)
    floppySyncManager.scheduleListItemRemove(itemId, Mode.fromType(itemType), listItem.list.idFloppy)
  }
}
