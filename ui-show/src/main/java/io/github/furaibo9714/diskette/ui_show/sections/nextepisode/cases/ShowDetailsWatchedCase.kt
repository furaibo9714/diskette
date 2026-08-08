package io.github.furaibo9714.diskette.ui_show.sections.nextepisode.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesLocalDataSource: EpisodesLocalDataSource,
) {

  suspend fun isWatched(
    show: Show,
    episode: Episode,
  ): Boolean =
    withContext(dispatchers.IO) {
      return@withContext episodesLocalDataSource.isEpisodeWatched(
        showTraktId = show.mediaId,
        episodeTraktId = episode.ids.media.key,
      )
    }
}
