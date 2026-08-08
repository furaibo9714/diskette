package io.github.furaibo9714.diskette.ui_progress.main.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.repository.EpisodesManager
import io.github.furaibo9714.diskette.repository.settings.SettingsSpoilersRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.EpisodeBundle
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class ProgressMainEpisodesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesManager: EpisodesManager,
  private val floppySyncManager: FloppySyncManager,
  private val spoilersSettings: SettingsSpoilersRepository,
  private val localDataSource: EpisodesLocalDataSource,
) {

  suspend fun setEpisodeWatched(
    bundle: EpisodeBundle,
    customDate: ZonedDateTime?,
  ) {
    episodesManager.setEpisodeWatched(bundle, customDate)
    floppySyncManager.scheduleEpisodeWatched(bundle.show.ids, bundle.episode.season, bundle.episode.number, Operation.ADD)
  }

  suspend fun isWatched(
    show: Show,
    episode: Episode,
  ): Boolean {
    return withContext(dispatchers.IO) {
      // No need to query DB if spoilers settings are all off in that case.
      if (!(
          spoilersSettings.isEpisodesTitleHidden ||
            spoilersSettings.isEpisodesDescriptionHidden ||
            spoilersSettings.isEpisodesImageHidden ||
            spoilersSettings.isEpisodesRatingHidden
        )
      ) {
        return@withContext false
      }
      return@withContext localDataSource.isEpisodeWatched(show.mediaId, episode.ids.media.id)
    }
  }
}
