package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_base.notifications.AnnouncementManager
import io.github.furaibo9714.diskette.ui_model.MediaId
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
    mediaId: MediaId,
    customDate: ZonedDateTime? = null,
  ) = withContext(dispatchers.IO) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))

    moviesRepository.myMovies.insert(mediaId, customDate)
    pinnedItemsRepository.removePinnedItem(movie)
    announcementManager.refreshMoviesAnnouncements()
    floppySyncManager.scheduleMovieWatched(mediaId, Operation.ADD)
  }

  suspend fun removeFromMyMovies(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(mediaId))
      moviesRepository.myMovies.delete(mediaId)
      pinnedItemsRepository.removePinnedItem(movie)
      floppySyncManager.scheduleMovieWatched(mediaId, Operation.REMOVE)
    }
}
