package io.github.furaibo9714.diskette.ui_gallery.fanart

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType

data class ArtGalleryUiState(
  val images: List<Image>? = null,
  val type: ImageType = ImageType.FANART,
  val pickedImage: Event<Image>? = null,
  val isLoading: Boolean = false,
)
