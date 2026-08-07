package io.github.furaibo9714.diskette.ui_gallery.fanart.recycler

import androidx.recyclerview.widget.DiffUtil
import io.github.furaibo9714.diskette.ui_model.Image

class ImageItemDiffCallback : DiffUtil.ItemCallback<Image>() {

  override fun areItemsTheSame(
    oldItem: Image,
    newItem: Image,
  ) = oldItem.id == newItem.id

  override fun areContentsTheSame(
    oldItem: Image,
    newItem: Image,
  ) = oldItem == newItem
}
