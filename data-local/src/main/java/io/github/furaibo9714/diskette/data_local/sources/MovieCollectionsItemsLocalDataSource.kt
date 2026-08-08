package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollectionItem

interface MovieCollectionsItemsLocalDataSource {

  suspend fun getById(collectionId: String): List<Movie>

  suspend fun deleteById(collectionId: String)

  suspend fun replace(
    collectionId: String,
    items: List<MovieCollectionItem>,
  )
}
