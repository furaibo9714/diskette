package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Rating

interface RatingsLocalDataSource {

  suspend fun getAll(): List<Rating>

  suspend fun getAllByType(type: String): List<Rating>

  suspend fun getAllByType(
    mediaIds: List<String>,
    type: String,
  ): List<Rating>

  suspend fun deleteByType(
    mediaId: String,
    type: String,
  )

  suspend fun replaceAll(
    ratings: List<Rating>,
    type: String,
  )

  suspend fun replace(rating: Rating)
}
