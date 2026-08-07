package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieCollection

interface MovieCollectionsLocalDataSource {

  suspend fun getById(traktId: Long): MovieCollection?

  suspend fun getByMovieId(movieTraktId: Long): List<MovieCollection>

  suspend fun replaceByMovieId(
    movieTraktId: Long,
    entities: List<MovieCollection>,
  )

  suspend fun insertAll(items: List<MovieCollection>)
}
