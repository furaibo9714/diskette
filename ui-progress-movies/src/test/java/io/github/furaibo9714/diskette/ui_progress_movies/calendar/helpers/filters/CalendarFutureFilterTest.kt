package io.github.furaibo9714.diskette.ui_progress_movies.calendar.helpers.filters

import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_progress_movies.BaseMockTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class CalendarFutureFilterTest : BaseMockTest() {

  private lateinit var SUT: CalendarFutureFilter

  @Before
  override fun setUp() {
    super.setUp()
    SUT = CalendarFutureFilter()
  }

  @Test
  fun `Should return true if release date is after now`() =
    runTest {
      val movie = Movie.EMPTY.copy(released = LocalDate.now().plusDays(3))
      val result = SUT.filter(ZonedDateTime.now(), movie)
      assertThat(result).isTrue()
    }

  @Test
  fun `Should return true if release date is today`() =
    runTest {
      val movie = Movie.EMPTY.copy(released = LocalDate.now())
      val result = SUT.filter(ZonedDateTime.now(), movie)
      assertThat(result).isTrue()
    }

  @Test
  fun `Should return false if release date is before today`() =
    runTest {
      val movie = Movie.EMPTY.copy(released = LocalDate.now().minusDays(1))
      val result = SUT.filter(ZonedDateTime.now(), movie)
      assertThat(result).isFalse()
    }
}
