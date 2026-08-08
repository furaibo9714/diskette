package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollection as MovieCollectionEntity
import io.github.furaibo9714.diskette.data_remote.media.model.MovieCollection as MovieCollectionNetwork
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.MovieCollection
import java.time.ZonedDateTime
import javax.inject.Inject

class CollectionMapper @Inject constructor() {

  fun fromNetwork(input: MovieCollectionNetwork): MovieCollection =
    MovieCollection(
      id = MediaId.parse(input.ids.media!!),
      name = input.name,
      description = input.description,
      itemCount = input.item_count,
    )

  fun fromEntity(input: MovieCollectionEntity): MovieCollection =
    MovieCollection(
      id = MediaId.parse(input.mediaId),
      name = input.name,
      description = input.description,
      itemCount = input.itemCount,
    )

  fun toEntity(
    movieId: Long,
    input: MovieCollection,
    updatedAt: ZonedDateTime = nowUtc(),
    createdAt: ZonedDateTime = nowUtc(),
  ): MovieCollectionEntity =
    MovieCollectionEntity(
      mediaId = input.id.id,
      movieMediaId = movieId,
      name = input.name,
      description = input.description,
      itemCount = input.itemCount,
      updatedAt = updatedAt,
      createdAt = createdAt,
    )
}
