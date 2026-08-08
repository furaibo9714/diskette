package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.database.model.ShowSearch

interface ShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAllForSearch(): List<ShowSearch>

  suspend fun getAll(ids: List<String>): List<Show>

  suspend fun getAllTmdbIds(mediaIds: List<String>): Map<String, Long>

  suspend fun getAllChunked(ids: List<String>): List<Show>

  suspend fun getById(mediaId: String): Show?

  suspend fun getByTmdbId(tmdbId: Long): Show?

  suspend fun getBySlug(slug: String): Show?

  suspend fun getByImdbId(imdbId: String): Show?

  suspend fun deleteById(mediaId: String)

  suspend fun upsert(shows: List<Show>)
}
