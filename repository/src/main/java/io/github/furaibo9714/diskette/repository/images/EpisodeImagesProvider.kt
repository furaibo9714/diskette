package io.github.furaibo9714.diskette.repository.images

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageFamily.EPISODE
import io.github.furaibo9714.diskette.ui_model.ImageSource
import io.github.furaibo9714.diskette.ui_model.ImageStatus.AVAILABLE
import io.github.furaibo9714.diskette.ui_model.ImageStatus.UNAVAILABLE
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.ImageType.FANART
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeImagesProvider @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadRemoteImage(
    showId: IdTmdb,
    episode: Episode,
  ): Image =
    withContext(dispatchers.IO) {
      val cachedImage = findCachedImage(episode, FANART)
      if (cachedImage.status == AVAILABLE) {
        return@withContext cachedImage
      }

      var image = Image.createUnavailable(FANART)
      try {
        var remoteImage = remoteSource.tmdb.fetchEpisodeImage(showId.id, episode.season, episode.number)
        if (remoteImage == null && (episode.numberAbs ?: 0) > 0) {
          // Try absolute episode number if present (may happen with certain Anime series)
          remoteImage = remoteSource.tmdb.fetchEpisodeImage(showId.id, episode.season, episode.numberAbs)
        }
        image = when (remoteImage) {
          null -> Image.createUnavailable(FANART)
          else -> Image(
            id = -1,
            idTvdb = episode.ids.tvdb,
            idTmdb = episode.ids.tmdb,
            type = FANART,
            family = EPISODE,
            fileUrl = remoteImage.file_path,
            thumbnailUrl = "",
            status = AVAILABLE,
            source = ImageSource.TMDB,
          )
        }
      } catch (error: Throwable) {
        Timber.w(error)
      }

      when (image.status) {
        UNAVAILABLE -> {
          localSource.showImages.deleteByEpisodeId(
            id = episode.ids.tmdb.id,
            type = image.type.key,
          )
        }
        else -> {
          localSource.showImages.insertEpisodeImage(mappers.image.toDatabaseShow(image))
        }
      }

      return@withContext image
    }

  private suspend fun findCachedImage(
    episode: Episode,
    type: ImageType,
  ): Image =
    when (val image = localSource.showImages.getByEpisodeId(episode.ids.tmdb.id, type.key)) {
      null -> Image.createUnknown(type, EPISODE)
      else -> mappers.image.fromDatabase(image).copy(type = type)
    }
}
