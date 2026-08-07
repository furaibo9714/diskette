package io.github.furaibo9714.diskette.ui_discover_movies.helpers.itemtype

import io.github.furaibo9714.diskette.ui_model.ImageType

internal interface ImageTypeProvider {
  fun getImageType(position: Int): ImageType
}
