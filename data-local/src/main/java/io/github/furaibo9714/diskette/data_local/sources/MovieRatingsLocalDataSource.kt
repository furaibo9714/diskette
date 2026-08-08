package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieRatings

interface MovieRatingsLocalDataSource {

  suspend fun upsert(entity: MovieRatings)

  suspend fun getById(mediaId: String): MovieRatings?
}
