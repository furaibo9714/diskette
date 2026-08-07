package io.github.furaibo9714.diskette.ui_people.gallery.cases

import io.github.furaibo9714.diskette.repository.images.PeopleImagesProvider
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.Image
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonGalleryImagesCase @Inject constructor(
  private val imagesProvider: PeopleImagesProvider,
) {

  suspend fun loadImages(id: IdTmdb): List<Image> {
    val initial = imagesProvider.loadCachedImage(id)
    val images = imagesProvider.loadImages(id).filter { it.fileUrl != initial?.fileUrl }
    return (listOf(initial) + images).filterNotNull()
  }
}
