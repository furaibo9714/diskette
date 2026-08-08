package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.data_local.sources.RatingsLocalDataSource

@Dao
interface RatingsDao :
  BaseDao<Rating>,
  RatingsLocalDataSource {

  @Query("SELECT * FROM ratings ORDER BY rated_at DESC")
  override suspend fun getAll(): List<Rating>

  @Query("SELECT * FROM ratings WHERE type == :type ORDER BY rated_at DESC")
  override suspend fun getAllByType(type: String): List<Rating>

  @Query("SELECT * FROM ratings WHERE media_id IN (:mediaIds) AND type == :type ORDER BY rated_at DESC")
  override suspend fun getAllByType(
    mediaIds: List<String>,
    type: String,
  ): List<Rating>

  @Query("DELETE FROM ratings WHERE type == :type AND media_id IN (:ids)")
  suspend fun deleteAllByType(
    type: String,
    ids: Set<String>,
  )

  @Query("DELETE FROM ratings WHERE media_id == :mediaId AND type == :type")
  override suspend fun deleteByType(
    mediaId: String,
    type: String,
  )

  @Transaction
  override suspend fun replaceAll(
    ratings: List<Rating>,
    type: String,
  ) {
    deleteAllByType(type, ratings.map { it.mediaId }.toSet())
    insert(ratings)
  }

  @Transaction
  override suspend fun replace(rating: Rating) {
    deleteByType(rating.mediaId, rating.type)
    insert(listOf(rating))
  }
}
