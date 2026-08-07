package io.github.furaibo9714.diskette.ui_progress_movies

import io.github.furaibo9714.diskette.common_test.MainDispatcherRule
import io.github.furaibo9714.diskette.common_test.UnconfinedCoroutineDispatchers
import io.mockk.MockKAnnotations
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  protected val testDispatchers = UnconfinedCoroutineDispatchers()

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic("androidx.room.RoomDatabaseKt")
  }
}
