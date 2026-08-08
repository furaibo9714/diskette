package io.github.furaibo9714.diskette.ui_movie.sections.related.recycler

import androidx.recyclerview.widget.DiffUtil

class RelatedItemDiffCallback : DiffUtil.ItemCallback<RelatedListItem>() {

  override fun areItemsTheSame(
    oldItem: RelatedListItem,
    newItem: RelatedListItem,
  ) = oldItem.movie.ids.media == newItem.movie.ids.media

  override fun areContentsTheSame(
    oldItem: RelatedListItem,
    newItem: RelatedListItem,
  ) = oldItem.image == newItem.image &&
    oldItem.isLoading == newItem.isLoading &&
    oldItem.isFollowed == newItem.isFollowed &&
    oldItem.isWatchlist == newItem.isWatchlist
}
