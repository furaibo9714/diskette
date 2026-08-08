package io.github.furaibo9714.diskette.ui_search.cases

import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.media.model.SearchResult
import io.github.furaibo9714.diskette.data_remote.media.model.Show
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class SearchQueryCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var cloud: RemoteDataSource
  @RelaxedMockK lateinit var traktApi: MediaRemoteDataSource
  @RelaxedMockK lateinit var mappers: Mappers
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var showsRepository: ShowsRepository
  @RelaxedMockK lateinit var moviesRepository: MoviesRepository
  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository
  @RelaxedMockK lateinit var showImagesProvider: ShowImagesProvider
  @RelaxedMockK lateinit var movieImagesProvider: MovieImagesProvider

  private lateinit var SUT: SearchQueryCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { cloud.media } returns traktApi
    coEvery { settingsRepository.isMoviesEnabled } returns true
    coEvery { translationsRepository.getLanguage() } returns "en"

    coEvery { showImagesProvider.findCachedImage(any(), any()) } returns Image.createUnknown(ImageType.POSTER)
    coEvery { movieImagesProvider.findCachedImage(any(), any()) } returns Image.createUnknown(ImageType.POSTER)

    coEvery { showsRepository.myShows.loadAllIds() } returns emptyList()
    coEvery { showsRepository.watchlistShows.loadAllIds() } returns emptyList()
    coEvery { moviesRepository.myMovies.loadAllIds() } returns emptyList()
    coEvery { moviesRepository.watchlistMovies.loadAllIds() } returns emptyList()

    SUT = SearchQueryCase(
      testDispatchers,
      cloud,
      mappers,
      settingsRepository,
      showsRepository,
      moviesRepository,
      translationsRepository,
      showImagesProvider,
      movieImagesProvider,
    )
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should run search query and return results sorted by order`() =
    runTest {
      val show = mockk<Show> {
        coEvery { votes } returnsMany listOf(10, 20, 30)
      }
      val item1 = SearchResult(order = 3, score = 1F, show = show, movie = null)
      val item2 = SearchResult(order = 2, score = 2F, show = show, movie = null)
      val item3 = SearchResult(order = 1, score = 3F, show = show, movie = null)

      coEvery { traktApi.fetchSearch(any(), any()) } returns listOf(item1, item2, item3)

      val result = SUT.searchByQuery("test")

      assertThat(result).hasSize(3)
      assertThat(result[0].order).isEqualTo(1)
      assertThat(result[1].order).isEqualTo(2)
      assertThat(result[2].order).isEqualTo(3)
      coVerify(exactly = 1) { traktApi.fetchSearch("test", any()) }
      coVerify(exactly = 3) { showImagesProvider.findCachedImage(any(), any()) }
      coVerify(exactly = 0) { movieImagesProvider.findCachedImage(any(), any()) }
    }
}
