package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollectionItem

interface MovieCollectionsItemsLocalDataSource {

  suspend fun getById(collectionId: Long): List<Movie>

  suspend fun deleteById(collectionId: Long)

  suspend fun replace(
    collectionId: Long,
    items: List<MovieCollectionItem>,
  )
}
