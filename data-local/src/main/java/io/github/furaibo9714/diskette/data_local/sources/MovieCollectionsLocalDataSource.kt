package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieCollection

interface MovieCollectionsLocalDataSource {

  suspend fun getById(mediaId: String): MovieCollection?

  suspend fun getByMovieId(movieMediaId: String): List<MovieCollection>

  suspend fun replaceByMovieId(
    movieMediaId: String,
    entities: List<MovieCollection>,
  )

  suspend fun insertAll(items: List<MovieCollection>)
}
