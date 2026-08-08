package io.github.furaibo9714.diskette.ui_show.sections.nextepisode.cases

import dagger.hilt.android.scopes.ViewModelScoped
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.MediaId
import javax.inject.Inject
import kotlinx.coroutines.withContext

@ViewModelScoped
class ShowDetailsNextEpisodeCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadNextEpisode(mediaId: MediaId): Episode? =
    withContext(dispatchers.IO) {
      // A show with no TMDB id is a Floppy manual entry, which has no next episode to look up.
      val tmdbId = mediaId.tmdbIdOrNull ?: return@withContext null
      val episode = remoteSource.media.fetchNextEpisode(tmdbId) ?: return@withContext null
      return@withContext mappers.episode.fromNetwork(episode)
    }
}
