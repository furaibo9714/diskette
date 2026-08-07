package io.github.furaibo9714.diskette.ui_show

import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.RatingState
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation

data class ShowDetailsUiState(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val listsCount: Int? = null,
  val followedState: FollowedState? = null,
  val ratingState: RatingState? = null,
  val translation: Translation? = null,
  val spoilers: SpoilersSettings? = null,
) {

  data class FollowedState(
    val isMyShows: Boolean,
    val isWatchlist: Boolean,
    val isHidden: Boolean,
    val withAnimation: Boolean,
  ) {

    fun isInCollection() = isMyShows || isWatchlist || isHidden

    companion object {
      fun idle() = FollowedState(isMyShows = false, isWatchlist = false, isHidden = false, withAnimation = true)

      fun inMyShows() = FollowedState(isMyShows = true, isWatchlist = false, isHidden = false, withAnimation = true)

      fun inWatchlist() = FollowedState(isMyShows = false, isWatchlist = true, isHidden = false, withAnimation = true)

      fun inHidden() = FollowedState(isMyShows = false, isWatchlist = false, isHidden = true, withAnimation = true)
    }
  }
}
