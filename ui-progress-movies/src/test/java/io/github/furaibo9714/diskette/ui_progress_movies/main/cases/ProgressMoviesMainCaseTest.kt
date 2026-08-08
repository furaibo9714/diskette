package io.github.furaibo9714.diskette.ui_progress_movies.main.cases

import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Operation
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesMainCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var moviesRepository: MoviesRepository
  @RelaxedMockK lateinit var pinnedItemsRepository: PinnedItemsRepository
  @RelaxedMockK lateinit var floppySyncManager: FloppySyncManager

  private lateinit var SUT: ProgressMoviesMainCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = ProgressMoviesMainCase(
      moviesRepository,
      pinnedItemsRepository,
      floppySyncManager,
    )
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should add movie to movies history properly`() =
    runTest {
      val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(media = MediaId.parse(123)))

      SUT.addToMyMovies(movie, null)

      coVerify { moviesRepository.myMovies.insert(MediaId.parse(123), null) }
      coVerify { pinnedItemsRepository.removePinnedItem(movie) }
      coVerify { floppySyncManager.scheduleMovieWatched(MediaId.parse(123), Operation.ADD) }
    }

  @Test
  fun `Should add movie to movies history properly using only ID`() =
    runTest {
      SUT.addToMyMovies(MediaId.parse(123))

      coVerify { moviesRepository.myMovies.insert(MediaId.parse(123), null) }
      coVerify { pinnedItemsRepository.removePinnedItem(any<Movie>()) }
      coVerify { floppySyncManager.scheduleMovieWatched(MediaId.parse(123), Operation.ADD) }
    }
}
