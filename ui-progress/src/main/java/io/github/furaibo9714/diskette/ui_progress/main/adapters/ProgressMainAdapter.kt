@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.ui_progress.main.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.furaibo9714.diskette.ui_progress.R
import io.github.furaibo9714.diskette.ui_progress.calendar.CalendarFragment
import io.github.furaibo9714.diskette.ui_progress.history.HistoryFragment
import io.github.furaibo9714.diskette.ui_progress.progress.ProgressFragment

class ProgressMainAdapter(
  fragManager: FragmentManager,
  private val context: Context,
) : FragmentPagerAdapter(fragManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  companion object {
    const val PAGES_COUNT = 3
  }

  override fun getItem(position: Int): Fragment =
    when (position) {
      0 -> ProgressFragment()
      1 -> CalendarFragment()
      2 -> HistoryFragment()
      else -> throw IllegalStateException("Unknown position")
    }

  override fun getCount() = PAGES_COUNT

  override fun getPageTitle(position: Int) =
    when (position) {
      0 -> context.getString(R.string.tabProgress)
      1 -> context.getString(R.string.tabCalendar)
      2 -> context.getString(R.string.tabHistory)
      else -> throw IllegalStateException()
    }
}
