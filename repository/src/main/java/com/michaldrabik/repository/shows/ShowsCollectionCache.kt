package com.michaldrabik.repository.shows

import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared by [MyShowsRepository], [WatchlistShowsRepository] and [HiddenShowsRepository] so their
 * `loadAll()` results aren't independently re-fetched/re-mapped from Room by every caller (Progress,
 * Calendar, History, sync runners, ...) on every load. Invalidated as a single unit on any write
 * from any of the three repositories, since their inserts move rows between each other's tables
 * directly rather than through each other's public API - per-repo invalidation would go stale
 * whenever an item moves between lists.
 */
@Singleton
class ShowsCollectionCache @Inject constructor() {
  @Volatile var myShows: List<Show>? = null
  @Volatile var watchlistShows: List<Show>? = null
  @Volatile var hiddenShows: List<Show>? = null

  fun invalidate() {
    myShows = null
    watchlistShows = null
    hiddenShows = null
  }
}
