package io.github.furaibo9714.diskette.ui_my_movies.mymovies.recycler

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import io.github.furaibo9714.diskette.common.Config.LISTS_GRID_SPAN
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode.LIST_COMPACT
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode.LIST_GRID
import io.github.furaibo9714.diskette.ui_base.common.ListViewMode.LIST_NORMAL
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.isTablet

internal object MyMoviesLayoutManagerProvider {

  fun provideLayoutManger(
    context: Context,
    viewMode: ListViewMode,
    gridSpanSize: Int,
  ): RecyclerView.LayoutManager =
    if (context.isTablet()) {
      provideTabletLayout(context, viewMode, gridSpanSize)
    } else {
      providePhoneLayout(context, viewMode)
    }

  private fun providePhoneLayout(
    context: Context,
    viewMode: ListViewMode,
  ): RecyclerView.LayoutManager =
    when (viewMode) {
      LIST_NORMAL -> LinearLayoutManager(context, VERTICAL, false)
      LIST_GRID -> GridLayoutManager(context, LISTS_GRID_SPAN)
      LIST_COMPACT -> LinearLayoutManager(context, VERTICAL, false)
    }

  private fun provideTabletLayout(
    context: Context,
    viewMode: ListViewMode,
    gridSpanSize: Int,
  ): RecyclerView.LayoutManager =
    when (viewMode) {
      LIST_NORMAL -> GridLayoutManager(context, gridSpanSize)
      LIST_GRID -> GridLayoutManager(context, LISTS_GRID_SPAN * 2)
      LIST_COMPACT -> GridLayoutManager(context, gridSpanSize)
    }
}
