@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.ui_my_shows.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import io.github.furaibo9714.diskette.ui_my_shows.R
import io.github.furaibo9714.diskette.ui_my_shows.hidden.HiddenFragment
import io.github.furaibo9714.diskette.ui_my_shows.myshows.MyShowsFragment
import io.github.furaibo9714.diskette.ui_my_shows.watchlist.WatchlistFragment

class FollowedPagesAdapter(
  fragManager: FragmentManager,
  private val context: Context,
) : FragmentStatePagerAdapter(fragManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  companion object {
    const val PAGES_COUNT = 3
  }

  override fun getCount() = PAGES_COUNT

  override fun getItem(position: Int): Fragment =
    when (position) {
      0 -> MyShowsFragment()
      1 -> WatchlistFragment()
      2 -> HiddenFragment()
      else -> throw IllegalStateException("Unknown position")
    }

  override fun getPageTitle(position: Int) =
    when (position) {
      0 -> context.getString(R.string.menuMyShows)
      1 -> context.getString(R.string.menuWatchlist)
      2 -> context.getString(R.string.menuHidden)
      else -> throw IllegalStateException()
    }
}
