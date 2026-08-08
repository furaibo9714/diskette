package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollection
import io.github.furaibo9714.diskette.data_local.sources.MovieCollectionsLocalDataSource

@Dao
interface MovieCollectionsDao :
  BaseDao<MovieCollection>,
  MovieCollectionsLocalDataSource {

  @Query("SELECT * FROM movies_collections WHERE media_id == :mediaId")
  override suspend fun getById(mediaId: String): MovieCollection?

  @Query("SELECT * FROM movies_collections WHERE movie_media_id == :movieMediaId")
  override suspend fun getByMovieId(movieMediaId: String): List<MovieCollection>

  @Transaction
  override suspend fun replaceByMovieId(
    movieMediaId: String,
    entities: List<MovieCollection>,
  ) {
    val deleteCollections = getByMovieId(movieMediaId).map { it.mediaId }

    deleteCollectionsItems(deleteCollections)
    deleteCollections(deleteCollections)

    insert(entities)
  }

  override suspend fun insertAll(items: List<MovieCollection>) {
    insert(items)
  }

  @Query("DELETE FROM movies_collections WHERE media_id IN (:collectionIds)")
  suspend fun deleteCollections(collectionIds: List<String>)

  @Query("DELETE FROM movies_collections_items WHERE collection_media_id IN (:collectionIds)")
  suspend fun deleteCollectionsItems(collectionIds: List<String>)
}
