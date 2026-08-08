package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MovieSearch

interface MoviesLocalDataSource {

  suspend fun getAll(): List<Movie>

  suspend fun getAllForSearch(): List<MovieSearch>

  suspend fun getAll(ids: List<String>): List<Movie>

  suspend fun getAllTmdbIds(mediaIds: List<String>): Map<String, Long>

  suspend fun getAllChunked(ids: List<String>): List<Movie>

  suspend fun getById(mediaId: String): Movie?

  suspend fun getByTmdbId(tmdbId: Long): Movie?

  suspend fun getBySlug(slug: String): Movie?

  suspend fun getByImdbId(imdbId: String): Movie?

  suspend fun deleteById(mediaId: String)

  suspend fun upsert(movies: List<Movie>)
}
