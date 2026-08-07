package io.github.furaibo9714.diskette.repository

import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.dao.DiscoverShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowsDao
import io.github.furaibo9714.diskette.data_local.database.model.DiscoverShow
import io.github.furaibo9714.diskette.repository.common.BaseMockTest
import io.github.furaibo9714.diskette.repository.shows.DiscoverShowsRepository
import io.github.furaibo9714.diskette.ui_model.Show
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb

class DiscoverShowsRepositoryTest : BaseMockTest() {

  @MockK lateinit var showsDao: ShowsDao
  @MockK lateinit var discoverShowsDao: DiscoverShowsDao

  private lateinit var SUT: DiscoverShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = DiscoverShowsRepository(cloud, database, transactions, mappers)
    coEvery { database.shows } returns showsDao
    coEvery { database.discoverShows } returns discoverShowsDao
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(showsDao)
    confirmVerified(discoverShowsDao)
  }

  @Test
  fun `Should return true if cache is valid`() {
    runBlocking {
      val discoverShow = mockk<DiscoverShow> {
        every { createdAt } returns nowUtcMillis() - TimeUnit.HOURS.toMillis(6)
      }
      coEvery { discoverShowsDao.getMostRecent() } returns discoverShow

      assertThat(SUT.isCacheValid()).isTrue()
      coVerify(exactly = 1) { discoverShowsDao.getMostRecent() }
    }
  }

  @Test
  fun `Should return false if cache is not valid`() {
    runBlocking {
      val discoverShow = mockk<DiscoverShow> {
        every { createdAt } returns nowUtcMillis() - TimeUnit.HOURS.toMillis(13)
      }
      coEvery { discoverShowsDao.getMostRecent() } returns discoverShow

      assertThat(SUT.isCacheValid()).isFalse()
      coVerify(exactly = 1) { discoverShowsDao.getMostRecent() }
    }
  }

  @Test
  fun `Should load cached shows`() {
    runBlocking {
      val discoverShow = mockk<DiscoverShow> {
        every { idTrakt } returns 10
      }
      val showDb = mockk<ShowDb> {
        every { idTrakt } returns 10
      }

      coEvery { discoverShowsDao.getAll() } returns listOf(discoverShow)
      coEvery { showsDao.getAll(any()) } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      val shows = SUT.loadAllCached()

      assertThat(shows).hasSize(1)
      coVerify(exactly = 1) { discoverShowsDao.getAll() }
      coVerify(exactly = 1) { showsDao.getAll(any()) }
    }
  }
}
