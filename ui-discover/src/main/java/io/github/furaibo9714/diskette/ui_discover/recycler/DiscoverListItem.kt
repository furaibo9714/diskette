package io.github.furaibo9714.diskette.ui_discover.recycler

import io.github.furaibo9714.diskette.ui_base.common.ListItem
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation

data class DiscoverListItem(
  override val show: Show,
  override val image: Image,
  override var isLoading: Boolean = false,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false,
  val translation: Translation? = null,
) : ListItem
