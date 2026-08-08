package io.github.furaibo9714.diskette.ui_search.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SearchInvalidateItemCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
) {

  suspend fun checkFollowedState(item: SearchListItem) =
    withContext(dispatchers.IO) {
      when {
        item.isShow -> {
          val (isMy, isWatchlist) = awaitAll(
            async { showsRepository.myShows.exists(item.show.ids.media) },
            async { showsRepository.watchlistShows.exists(item.show.ids.media) },
          )
          Pair(isMy, isWatchlist)
        }
        item.isMovie -> {
          val (isMy, isWatchlist) = awaitAll(
            async { moviesRepository.myMovies.exists(item.movie.ids.media) },
            async { moviesRepository.watchlistMovies.exists(item.movie.ids.media) },
          )
          Pair(isMy, isWatchlist)
        }
        else -> {
          throw IllegalStateException()
        }
      }
    }
}
