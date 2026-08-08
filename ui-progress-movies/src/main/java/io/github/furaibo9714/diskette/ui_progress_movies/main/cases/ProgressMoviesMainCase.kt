package io.github.furaibo9714.diskette.ui_progress_movies.main.cases

import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.MediaId
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
    moviesRepository.myMovies.insert(movie.ids.media, customDate)
    pinnedItemsRepository.removePinnedItem(movie)
    floppySyncManager.scheduleMovieWatched(movie.ids.media, Operation.ADD)
  }

  suspend fun addToMyMovies(movieId: MediaId) {
    addToMyMovies(
      movie = Movie.EMPTY.copy(Ids.EMPTY.copy(media = movieId)),
      customDate = null,
    )
  }
}
