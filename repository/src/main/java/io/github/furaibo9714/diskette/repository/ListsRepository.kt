package io.github.furaibo9714.diskette.repository

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.CustomList
import io.github.furaibo9714.diskette.ui_model.MediaId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListsRepository @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val transactions: TransactionsProvider,
) {

  suspend fun createList(
    name: String,
    description: String?,
    idFloppy: Long?,
    idSlug: String?,
  ): CustomList {
    val list = CustomList.create().copy(
      idFloppy = idFloppy,
      idSlug = idSlug ?: "",
      name = name.trim(),
      description = description?.trim(),
    )
    val listDb = mappers.customList.toDatabase(list)
    localSource.customLists.insert(listOf(listDb))
    return list
  }

  suspend fun updateList(
    id: Long,
    idFloppy: Long?,
    idSlug: String?,
    name: String,
    description: String?,
  ): CustomList {
    val listDb = localSource.customLists.getById(id)!!
    val updated = listDb.copy(
      name = name,
      idFloppy = idFloppy ?: listDb.idFloppy,
      idSlug = idSlug ?: listDb.idSlug,
      description = description,
      updatedAt = nowUtcMillis(),
    )
    localSource.customLists.update(listOf(updated))
    return mappers.customList.fromDatabase(updated)
  }

  suspend fun deleteList(listId: Long) = localSource.customLists.deleteById(listId)

  suspend fun addToList(
    listId: Long,
    itemTraktId: MediaId,
    itemType: String,
    listedAt: Long = nowUtcMillis(),
    createdAt: Long = nowUtcMillis(),
    updatedAt: Long = nowUtcMillis(),
  ) {
    val itemDb = CustomListItem(
      rank = 0,
      idList = listId,
      mediaId = itemTraktId.key,
      type = itemType,
      listedAt = listedAt,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
    transactions.withTransaction {
      localSource.customListsItems.insertItem(itemDb)
      localSource.customLists.updateTimestamp(listId, nowUtcMillis())
    }
  }

  suspend fun removeFromList(
    listId: Long,
    itemTraktId: MediaId,
    itemType: String,
  ) {
    transactions.withTransaction {
      localSource.customListsItems.deleteItem(listId, itemTraktId.key, itemType)
      localSource.customLists.updateTimestamp(listId, nowUtcMillis())
    }
  }

  suspend fun loadListIdsForItem(
    itemTraktId: MediaId,
    itemType: String,
  ) = localSource.customListsItems.getListsForItem(itemTraktId.key, itemType)

  suspend fun loadListItemsForId(listId: Long) = localSource.customListsItems.getItemsById(listId)

  suspend fun loadById(listId: Long): CustomList {
    val listDb = localSource.customLists.getById(listId)!!
    return mappers.customList.fromDatabase(listDb)
  }

  suspend fun loadItemsById(listId: Long) = localSource.customListsItems.getItemsById(listId)

  suspend fun loadAll(): List<CustomList> {
    val listsDb = localSource.customLists.getAll()
    return listsDb.map { mappers.customList.fromDatabase(it) }
  }
}
