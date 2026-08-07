package io.github.furaibo9714.diskette.ui_show.sections.nextepisode.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsNextEpisodeCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? =
    withContext(dispatchers.IO) {
      val episode = remoteSource.trakt.fetchNextEpisode(traktId.id) ?: return@withContext null
      return@withContext mappers.episode.fromNetwork(episode)
    }
}
