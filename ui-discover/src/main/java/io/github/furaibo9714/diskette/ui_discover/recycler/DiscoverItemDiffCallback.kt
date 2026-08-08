package io.github.furaibo9714.diskette.ui_discover.recycler

import androidx.recyclerview.widget.DiffUtil

class DiscoverItemDiffCallback : DiffUtil.ItemCallback<DiscoverListItem>() {

  override fun areItemsTheSame(
    oldItem: DiscoverListItem,
    newItem: DiscoverListItem,
  ) = oldItem.show.ids.media == newItem.show.ids.media

  override fun areContentsTheSame(
    oldItem: DiscoverListItem,
    newItem: DiscoverListItem,
  ) = oldItem.image == newItem.image &&
    oldItem.isLoading == newItem.isLoading &&
    oldItem.isFollowed == newItem.isFollowed &&
    oldItem.isWatchlist == newItem.isWatchlist &&
    oldItem.translation == newItem.translation
}
