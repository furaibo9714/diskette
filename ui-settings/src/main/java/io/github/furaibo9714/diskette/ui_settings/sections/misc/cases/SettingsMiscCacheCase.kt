package io.github.furaibo9714.diskette.ui_settings.sections.misc.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsMiscCacheCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  suspend fun deleteImagesCache() {
    withContext(dispatchers.IO) {
      showsImagesProvider.deleteLocalCache()
      moviesImagesProvider.deleteLocalCache()
    }
  }
}
