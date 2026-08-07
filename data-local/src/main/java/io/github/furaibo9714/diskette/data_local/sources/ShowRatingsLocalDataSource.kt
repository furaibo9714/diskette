package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ShowRatings

interface ShowRatingsLocalDataSource {

  suspend fun upsert(entity: ShowRatings)

  suspend fun getById(traktId: Long): ShowRatings?
}
