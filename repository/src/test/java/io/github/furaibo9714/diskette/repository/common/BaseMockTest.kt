package io.github.furaibo9714.diskette.repository.common

import io.github.furaibo9714.diskette.common_test.UnconfinedCoroutineDispatchers
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.R
import io.github.furaibo9714.diskette.repository.mappers.CustomListMapper
import io.github.furaibo9714.diskette.repository.mappers.EpisodeMapper
import io.github.furaibo9714.diskette.repository.mappers.IdsMapper
import io.github.furaibo9714.diskette.repository.mappers.ImageMapper
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.mappers.MovieMapper
import io.github.furaibo9714.diskette.repository.mappers.PersonMapper
import io.github.furaibo9714.diskette.repository.mappers.RatingsMapper
import io.github.furaibo9714.diskette.repository.mappers.SeasonMapper
import io.github.furaibo9714.diskette.repository.mappers.SettingsMapper
import io.github.furaibo9714.diskette.repository.mappers.ShowMapper
import io.github.furaibo9714.diskette.repository.mappers.StreamingsMapper
import io.github.furaibo9714.diskette.repository.mappers.TranslationMapper
import io.github.furaibo9714.diskette.repository.mappers.UserRatingsMapper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
abstract class BaseMockTest {

  @MockK lateinit var database: LocalDataSource
  @MockK lateinit var transactions: TransactionsProvider
  @MockK lateinit var cloud: RemoteDataSource

  protected val testDispatchers = UnconfinedCoroutineDispatchers()
  private val idsMapper = IdsMapper()
  private val episodeMappers = EpisodeMapper(idsMapper)

  @SpyK var mappers = Mappers(
    idsMapper,
    ImageMapper(),
    ShowMapper(idsMapper),
    MovieMapper(idsMapper),
    episodeMappers,
    SeasonMapper(idsMapper, episodeMappers),
    PersonMapper(),
    SettingsMapper(),
    TranslationMapper(idsMapper),
    CustomListMapper(),
    RatingsMapper(),
    UserRatingsMapper(),
    StreamingsMapper(),
  )

  @Before
  open fun setUp() {
    MockKAnnotations.init(this)
    clearAllMocks()
    mockkStatic("androidx.room.RoomDatabaseKt")
    val lambda = slot<suspend () -> R>()
    coEvery { transactions.withTransaction(capture(lambda)) } coAnswers { lambda.captured.invoke() }
  }
}
