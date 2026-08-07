package io.github.furaibo9714.diskette.ui_progress_movies.progress

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_progress_movies.progress.recycler.ProgressMovieListItem

data class ProgressMoviesUiState(
  val items: List<ProgressMovieListItem>? = null,
  val scrollReset: Event<Boolean>? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
  val isOverScrollEnabled: Boolean = false,
)
