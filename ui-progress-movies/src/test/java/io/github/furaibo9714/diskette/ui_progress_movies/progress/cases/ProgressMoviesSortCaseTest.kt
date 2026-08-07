package io.github.furaibo9714.diskette.ui_progress_movies.progress.cases

import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesSortCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: ProgressMoviesSortCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = ProgressMoviesSortCase(settingsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should set sorting order properly`() =
    runTest {
      SUT.setSortOrder(SortOrder.RANK, SortType.DESCENDING)

      coVerify { settingsRepository.sorting setProperty "progressMoviesSortOrder" value SortOrder.RANK }
      coVerify { settingsRepository.sorting setProperty "progressMoviesSortType" value SortType.DESCENDING }
    }
}
