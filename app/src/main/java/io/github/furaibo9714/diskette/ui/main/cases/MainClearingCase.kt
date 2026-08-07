package io.github.furaibo9714.diskette.ui.main.cases

import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MainClearingCase @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  fun clear() {
    showImagesProvider.clear()
    movieImagesProvider.clear()
    Timber.d("Clearing...")
  }
}
