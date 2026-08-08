package io.github.furaibo9714.diskette.ui_show.quicksetup

import androidx.recyclerview.widget.DiffUtil

class QuickSetupItemDiffCallback : DiffUtil.ItemCallback<QuickSetupListItem>() {

  override fun areItemsTheSame(
    oldItem: QuickSetupListItem,
    newItem: QuickSetupListItem,
  ) = oldItem.episode.ids.media == newItem.episode.ids.media &&
    oldItem.season.ids.media == newItem.season.ids.media &&
    oldItem.isHeader == newItem.isHeader

  override fun areContentsTheSame(
    oldItem: QuickSetupListItem,
    newItem: QuickSetupListItem,
  ) = oldItem.episode == newItem.episode &&
    oldItem.season == newItem.season &&
    oldItem.isHeader == newItem.isHeader &&
    oldItem.isChecked == newItem.isChecked
}
