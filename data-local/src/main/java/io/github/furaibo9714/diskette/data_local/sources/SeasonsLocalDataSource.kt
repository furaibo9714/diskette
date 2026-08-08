package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Season

interface SeasonsLocalDataSource {

  suspend fun getAll(mediaIds: List<String>): List<Season>

  suspend fun getAllByShowsIds(mediaIds: List<String>): List<Season>

  suspend fun getAllByShowsIdsChunk(mediaIds: List<String>): List<Season>

  suspend fun getAllWatched(): List<Season>

  suspend fun getAllWatchedForShows(mediaIds: List<String>): List<Season>

  suspend fun getAllWatchedIdsForShows(mediaIds: List<String>): List<String>

  suspend fun getAllByShowId(mediaId: String): List<Season>

  suspend fun getById(mediaId: String): Season?

  suspend fun update(items: List<Season>)

  suspend fun upsert(items: List<Season>)

  suspend fun delete(items: List<Season>)

  suspend fun deleteAllForShow(showMediaId: String)
}
