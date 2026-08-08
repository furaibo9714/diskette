package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ArchiveMovie
import io.github.furaibo9714.diskette.data_local.database.model.Movie

interface ArchiveMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAll(ids: List<String>): List<Movie>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Movie?

  suspend fun insert(movie: ArchiveMovie)

  suspend fun deleteById(mediaId: String)
}
