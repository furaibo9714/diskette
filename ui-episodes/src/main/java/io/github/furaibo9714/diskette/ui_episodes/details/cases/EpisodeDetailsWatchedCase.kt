package io.github.furaibo9714.diskette.ui_episodes.details.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.MediaId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsWatchedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesDataSource: EpisodesLocalDataSource,
) {

  suspend fun getLastWatchedAt(
    showId: MediaId,
    episode: Episode,
  ): ZonedDateTime? =
    withContext(dispatchers.IO) {
      episodesDataSource.getById(showId.id, episode.ids.media.id)?.lastWatchedAt
    }
}
