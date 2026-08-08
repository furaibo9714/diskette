package io.github.furaibo9714.diskette.repository.movies.ratings

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.UserRating
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRatingsRepository @Inject constructor(
  val external: MoviesExternalRatingsRepository,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  companion object {
    private const val TYPE_MOVIE = "movie"
  }

  suspend fun loadMoviesRatings(): List<UserRating> {
    val ratings = localSource.ratings.getAllByType(TYPE_MOVIE)
    return ratings.map {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun loadRatings(movies: List<Movie>): List<UserRating> {
    val ratings = mutableListOf<Rating>()
    movies.chunked(250).forEach { chunk ->
      val items = localSource.ratings.getAllByType(chunk.map { it.mediaId.key }, TYPE_MOVIE)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun addRating(
    movie: Movie,
    rating: Int,
  ) {
    val ratedAt = nowUtc()
    val entity = mappers.userRatings.toDatabaseMovie(movie, rating, ratedAt)
    localSource.ratings.replace(entity)
  }

  suspend fun deleteRating(movie: Movie) {
    localSource.ratings.deleteByType(movie.mediaId.key, TYPE_MOVIE)
  }
}
