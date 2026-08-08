package io.github.furaibo9714.diskette.ui_base.common

import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Show

interface ListItem {
  val show: Show
  val image: Image
  val isLoading: Boolean

  infix fun isSameAs(other: ListItem) = show.ids.media == other.show.ids.media
}
