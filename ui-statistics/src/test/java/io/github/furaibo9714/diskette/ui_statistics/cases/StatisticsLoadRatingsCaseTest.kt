package io.github.furaibo9714.diskette.ui_statistics.cases

import BaseMockTest
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.UserRating
import io.github.furaibo9714.diskette.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class StatisticsLoadRatingsCaseTest : BaseMockTest() {

  @MockK lateinit var showsRepository: ShowsRepository
  @MockK lateinit var ratingsRepository: RatingsRepository
  @MockK lateinit var showImagesProvider: ShowImagesProvider

  private lateinit var SUT: StatisticsLoadRatingsCase

  @Before
  override fun setUp() {
    super.setUp()

    SUT = StatisticsLoadRatingsCase(
      showsRepository,
      ratingsRepository,
      showImagesProvider,
    )
  }

  @Test
  fun `Should load sorted ratings properly`() =
    runTest {
      val ratings = listOf(
        UserRating.EMPTY.copy(MediaId.parse(1), ratedAt = ZonedDateTime.of(2000, 3, 1, 1, 1, 1, 1, ZoneId.systemDefault())),
        UserRating.EMPTY.copy(MediaId.parse(2), ratedAt = ZonedDateTime.of(2001, 3, 1, 1, 1, 1, 1, ZoneId.systemDefault())),
        UserRating.EMPTY.copy(MediaId.parse(3), ratedAt = ZonedDateTime.of(2002, 3, 1, 1, 1, 1, 1, ZoneId.systemDefault())),
      )

      val shows = listOf(
        Show.EMPTY.copy(Ids.EMPTY.copy(trakt = MediaId.parse(1))),
        Show.EMPTY.copy(Ids.EMPTY.copy(trakt = MediaId.parse(2))),
        Show.EMPTY.copy(Ids.EMPTY.copy(trakt = MediaId.parse(3))),
        Show.EMPTY.copy(Ids.EMPTY.copy(trakt = MediaId.parse(4))),
        Show.EMPTY.copy(Ids.EMPTY.copy(trakt = MediaId.parse(5))),
      )

      val image = Image.createUnknown(ImageType.POSTER)

      coEvery { ratingsRepository.shows.loadShowsRatings() } returns ratings
      coEvery { showsRepository.myShows.loadAll(any()) } returns shows
      coEvery { showImagesProvider.findCachedImage(any(), any()) } returns image

      val result = SUT.loadRatings()

      assertThat(result).hasSize(3)
      assertThat(result).containsExactly(
        StatisticsRatingItem(shows[2], image, false, ratings[2]),
        StatisticsRatingItem(shows[1], image, false, ratings[1]),
        StatisticsRatingItem(shows[0], image, false, ratings[0]),
      )
    }
}
