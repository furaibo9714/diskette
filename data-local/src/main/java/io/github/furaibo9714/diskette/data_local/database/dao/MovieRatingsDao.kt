package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.MovieRatings
import io.github.furaibo9714.diskette.data_local.sources.MovieRatingsLocalDataSource

@Dao
interface MovieRatingsDao :
  BaseDao<MovieRatings>,
  MovieRatingsLocalDataSource {

  @Transaction
  override suspend fun upsert(entity: MovieRatings) {
    val local = getById(entity.idTrakt)
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

  @Query("SELECT * FROM movies_ratings WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): MovieRatings?
}
