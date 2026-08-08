package io.github.furaibo9714.diskette.ui_statistics_movies.views.ratings.recycler

import androidx.recyclerview.widget.DiffUtil

class StatisticsMoviesRatingsDiffCallback : DiffUtil.ItemCallback<StatisticsMoviesRatingItem>() {

  override fun areItemsTheSame(
    oldItem: StatisticsMoviesRatingItem,
    newItem: StatisticsMoviesRatingItem,
  ) = oldItem.movie.ids.media == newItem.movie.ids.media

  override fun areContentsTheSame(
    oldItem: StatisticsMoviesRatingItem,
    newItem: StatisticsMoviesRatingItem,
  ) = oldItem.rating.rating == newItem.rating.rating &&
    oldItem.image == newItem.image
}
