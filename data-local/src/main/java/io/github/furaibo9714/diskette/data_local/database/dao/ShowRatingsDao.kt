package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.ShowRatings
import io.github.furaibo9714.diskette.data_local.sources.ShowRatingsLocalDataSource

@Dao
interface ShowRatingsDao :
  BaseDao<ShowRatings>,
  ShowRatingsLocalDataSource {

  @Transaction
  override suspend fun upsert(entity: ShowRatings) {
    val local = getById(entity.mediaId)
    if (local != null) {
      update(
        listOf(
          local.copy(
            tmdb = entity.tmdb,
            updatedAt = entity.updatedAt,
          ),
        ),
      )
      return
    }
    insert(listOf(entity))
  }

  @Query("SELECT * FROM shows_ratings WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): ShowRatings?
}
