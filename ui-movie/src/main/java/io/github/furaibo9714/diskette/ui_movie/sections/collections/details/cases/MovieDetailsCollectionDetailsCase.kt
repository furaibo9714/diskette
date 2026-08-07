package io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MovieCollectionsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionDetailsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val collectionsRepository: MovieCollectionsRepository,
) {

  suspend fun loadCollection(collectionId: IdTrakt): MovieDetailsCollectionItem.HeaderItem =
    withContext(dispatchers.IO) {
      val collection = collectionsRepository.loadCollection(collectionId)
        ?: throw Error("Requested collection must be available at this point")

      return@withContext MovieDetailsCollectionItem.HeaderItem(
        title = collection.name,
        description = collection.description,
      )
    }
}
