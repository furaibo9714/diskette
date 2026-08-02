package com.michaldrabik.repository.movies

import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared by [MyMoviesRepository], [WatchlistMoviesRepository] and [HiddenMoviesRepository] so their
 * `loadAll()` results aren't independently re-fetched/re-mapped from Room by every caller on every
 * load. Invalidated as a single unit on any write from any of the three repositories, since their
 * inserts move rows between each other's tables directly rather than through each other's public
 * API - per-repo invalidation would go stale whenever an item moves between lists.
 */
@Singleton
class MoviesCollectionCache @Inject constructor() {
  @Volatile var myMovies: List<Movie>? = null
  @Volatile var watchlistMovies: List<Movie>? = null
  @Volatile var hiddenMovies: List<Movie>? = null

  fun invalidate() {
    myMovies = null
    watchlistMovies = null
    hiddenMovies = null
  }
}
