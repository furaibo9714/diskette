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
      id = input.ids.tmdb?.let { MediaId.tmdb(it) } ?: MediaId.EMPTY,
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
    movieId: String,
    input: MovieCollection,
    updatedAt: ZonedDateTime = nowUtc(),
    createdAt: ZonedDateTime = nowUtc(),
  ): MovieCollectionEntity =
    MovieCollectionEntity(
      mediaId = input.id.key,
      movieMediaId = movieId,
      name = input.name,
      description = input.description,
      itemCount = input.itemCount,
      updatedAt = updatedAt,
      createdAt = createdAt,
    )
}
