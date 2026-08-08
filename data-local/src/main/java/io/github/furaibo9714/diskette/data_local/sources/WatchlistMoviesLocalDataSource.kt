package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistMovie

interface WatchlistMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Movie?

  suspend fun insert(movie: WatchlistMovie)

  suspend fun deleteById(mediaId: String)

  suspend fun checkExists(mediaId: String): Boolean
}
