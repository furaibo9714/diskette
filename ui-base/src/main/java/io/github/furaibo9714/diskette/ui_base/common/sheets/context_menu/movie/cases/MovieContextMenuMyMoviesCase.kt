package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuMyMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun moveToMyMovies(
    traktId: IdTrakt,
    customDate: ZonedDateTime? = null,
  ) = withContext(dispatchers.IO) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    moviesRepository.myMovies.insert(traktId, customDate)
    pinnedItemsRepository.removePinnedItem(movie)
    announcementManager.refreshMoviesAnnouncements()
    floppySyncManager.scheduleMovieWatched(traktId, Operation.ADD)
  }

  suspend fun removeFromMyMovies(traktId: IdTrakt) =
    withContext(dispatchers.IO) {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
      moviesRepository.myMovies.delete(traktId)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieWatched(traktId, Operation.REMOVE)
    }
}
