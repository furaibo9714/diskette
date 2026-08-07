package io.github.furaibo9714.diskette.ui_my_movies.watchlist.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class WatchlistRatingsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRatings(): Map<IdTrakt, TraktRating?> =
    withContext(dispatchers.IO) {
      ratingsRepository.movies.loadMoviesRatings().associateBy { it.idTrakt }
    }
}
