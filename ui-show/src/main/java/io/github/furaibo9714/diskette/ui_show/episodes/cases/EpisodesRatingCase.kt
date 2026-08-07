package io.github.furaibo9714.diskette.ui_show.episodes.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Season
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesRatingCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(episode: Episode) =
    withContext(dispatchers.IO) {
      ratingsRepository.shows.loadRating(episode)
    }

  suspend fun loadRating(season: Season) =
    withContext(dispatchers.IO) {
      ratingsRepository.shows.loadRating(season)
    }
}
