package io.github.furaibo9714.diskette.ui_show.sections.related.recycler

import io.github.furaibo9714.diskette.ui_base.common.ListItem
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Show

data class RelatedListItem(
  override val show: Show,
  override val image: Image,
  override var isLoading: Boolean = false,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false,
) : ListItem
