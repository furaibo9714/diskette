package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MyMovie

interface MyMoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAll(ids: List<String>): List<Movie>

  suspend fun getAllRecent(limit: Int): List<Movie>

  suspend fun getAllMediaIds(): List<String>

  suspend fun getById(mediaId: String): Movie?

  suspend fun insert(movies: List<MyMovie>)

  suspend fun deleteById(mediaId: String)

  suspend fun checkExists(mediaId: String): Boolean
}
