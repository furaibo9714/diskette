package io.github.furaibo9714.diskette.repository.images

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.PersonImage
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageFamily
import io.github.furaibo9714.diskette.ui_model.ImageSource
import io.github.furaibo9714.diskette.ui_model.ImageType
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleImagesProvider @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val remoteSource: RemoteDataSource,
) {

  suspend fun loadCachedImage(personTmdbId: IdTmdb): Image? =
    withContext(dispatchers.IO) {
      val localPerson = localSource.people.getById(personTmdbId.id)
      return@withContext localPerson?.image?.let {
        Image.createAvailable(
          ids = Ids.EMPTY,
          type = ImageType.PROFILE,
          family = ImageFamily.PROFILE,
          path = it,
          source = ImageSource.TMDB,
        )
      }
    }

  suspend fun loadImages(personTmdbId: IdTmdb): List<Image> =
    withContext(dispatchers.IO) {
      val localTimestamp = localSource.peopleImages.getTimestampForPerson(personTmdbId.id) ?: 0
      if (localTimestamp + Config.PEOPLE_IMAGES_CACHE_DURATION > nowUtcMillis()) {
        val local = localSource.peopleImages.getAll(personTmdbId.id)
        return@withContext local.map {
          Image.createAvailable(
            ids = Ids.EMPTY,
            type = ImageType.PROFILE,
            family = ImageFamily.PROFILE,
            path = it.filePath,
            source = ImageSource.TMDB,
          )
        }
      }

      val images = (remoteSource.tmdb.fetchPersonImages(personTmdbId.id).profiles ?: emptyList())
        .filter { it.file_path.isNotBlank() }
      val dbImages = images.map {
        PersonImage(
          id = 0,
          idTmdb = personTmdbId.id,
          filePath = it.file_path,
          createdAt = nowUtc(),
          updatedAt = nowUtc(),
        )
      }

      localSource.peopleImages.insertSingle(personTmdbId.id, dbImages)

      return@withContext images.map {
        Image.createAvailable(
          ids = Ids.EMPTY,
          type = ImageType.PROFILE,
          family = ImageFamily.PROFILE,
          path = it.file_path,
          source = ImageSource.TMDB,
        )
      }
    }
}
