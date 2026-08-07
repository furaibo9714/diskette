package io.github.furaibo9714.diskette.repository

import io.github.furaibo9714.diskette.repository.movies.ratings.MoviesRatingsRepository
import io.github.furaibo9714.diskette.repository.shows.ratings.ShowsRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingsRepository @Inject constructor(
  val shows: ShowsRatingsRepository,
  val movies: MoviesRatingsRepository,
)
