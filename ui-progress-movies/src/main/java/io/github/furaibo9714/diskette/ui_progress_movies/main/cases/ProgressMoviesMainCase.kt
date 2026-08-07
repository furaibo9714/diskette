package io.github.furaibo9714.diskette.ui_progress_movies.main.cases

import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressMoviesMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun addToMyMovies(
    movie: Movie,
    customDate: ZonedDateTime?,
  ) {
    moviesRepository.myMovies.insert(movie.ids.trakt, customDate)
    pinnedItemsRepository.removePinnedItem(movie)
    floppySyncManager.scheduleMovieWatched(movie.ids.trakt, Operation.ADD)
  }

  suspend fun addToMyMovies(movieId: IdTrakt) {
    addToMyMovies(
      movie = Movie.EMPTY.copy(Ids.EMPTY.copy(trakt = movieId)),
      customDate = null,
    )
  }
}
