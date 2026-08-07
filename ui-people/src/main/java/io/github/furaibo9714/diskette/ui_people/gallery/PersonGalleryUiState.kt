package io.github.furaibo9714.diskette.ui_people.gallery

import io.github.furaibo9714.diskette.ui_model.Image

data class PersonGalleryUiState(
  val images: List<Image>? = null,
  val isLoading: Boolean = false,
)
