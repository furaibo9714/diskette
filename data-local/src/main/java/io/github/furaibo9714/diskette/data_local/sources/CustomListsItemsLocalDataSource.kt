package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem

interface CustomListsItemsLocalDataSource {

  suspend fun update(items: List<CustomListItem>)

  suspend fun getListsForItem(
    mediaId: String,
    type: String,
  ): List<Long>

  suspend fun getByIdTrakt(
    idList: Long,
    mediaId: String,
    type: String,
  ): CustomListItem?

  suspend fun getItemsById(idList: Long): List<CustomListItem>

  suspend fun getItemsForListImages(
    idList: Long,
    limit: Int,
  ): List<CustomListItem>

  suspend fun getRankForList(idList: Long): Long?

  suspend fun insertItem(item: CustomListItem)

  suspend fun deleteItem(
    idList: Long,
    mediaId: String,
    type: String,
  )
}
