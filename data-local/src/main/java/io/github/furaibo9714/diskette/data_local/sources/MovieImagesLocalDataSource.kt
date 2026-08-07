package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieImage

interface MovieImagesLocalDataSource {

  suspend fun getByMovieId(
    tmdbId: Long,
    type: String,
  ): MovieImage?

  suspend fun insertMovieImage(image: MovieImage)

  suspend fun upsert(image: MovieImage)

  suspend fun deleteByMovieId(
    id: Long,
    type: String,
  )

  suspend fun deleteAll()
}
