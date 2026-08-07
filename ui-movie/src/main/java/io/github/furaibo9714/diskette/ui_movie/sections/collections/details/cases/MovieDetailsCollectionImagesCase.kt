package io.github.furaibo9714.diskette.ui_movie.sections.collections.details.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.MovieItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionImagesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val movieImagesProvider: MovieImagesProvider,
) {

  suspend fun loadMissingImage(
    item: MovieItem,
    force: Boolean,
  ): MovieItem =
    withContext(dispatchers.IO) {
      try {
        val image = movieImagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        return@withContext item.copy(image = image)
      } catch (error: Throwable) {
        Timber.w(error)
        return@withContext item.copy(image = Image.createUnavailable(item.image.type))
      }
    }
}
