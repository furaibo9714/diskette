package io.github.furaibo9714.diskette.repository

import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.dao.ShowsDao
import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.media.model.Show as ShowRemote
import io.github.furaibo9714.diskette.repository.common.BaseMockTest
import io.github.furaibo9714.diskette.repository.shows.ShowDetailsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ShowDetailsRepositoryTest : BaseMockTest() {

  @MockK lateinit var mediaRemoteSource: MediaRemoteDataSource
  @MockK lateinit var showsDao: ShowsDao

  private lateinit var SUT: ShowDetailsRepository

  @Before
  override fun setUp() {
    super.setUp()
    every { database.shows } returns showsDao
    every { cloud.media } returns mediaRemoteSource

    SUT = ShowDetailsRepository(cloud, database, transactions, mappers)
  }

  @Test
  fun `Should load cached show details on given conditions`() {
    runBlocking {
      val showDb = mockk<Show>(relaxed = true) {
        every { mediaId } returns "tmdb:1"
        every { runtime } returns 42
        every { updatedAt } returns nowUtcMillis() - 100
      }
      coEvery { showsDao.getById(any<String>()) } returns showDb

      val show = SUT.load(MediaId.tmdb(1), false)

      assertThat(show.ids.media).isEqualTo(MediaId.tmdb(1))
      coVerify(exactly = 1) { showsDao.getById(any<String>()) }
      coVerify(exactly = 0) { mediaRemoteSource.fetchShow(any<Long>()) }
    }
  }

  @Test
  fun `Should load remote show details if force flag is set`() {
    runBlocking {
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.tmdb } returns 1
      }
      coEvery { showsDao.getById(any<String>()) } returns null
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { mediaRemoteSource.fetchShow(any<Long>()) } returns showRemote

      val show = SUT.load(MediaId.tmdb(1), true)

      assertThat(show.ids.media).isEqualTo(MediaId.tmdb(1))

      coVerifySequence {
        showsDao.getById(any<String>())
        mediaRemoteSource.fetchShow(any<Long>())
        showsDao.upsert(any())
      }
    }
  }

  @Test
  fun `Should load remote show details if nothing is cached`() {
    runBlocking {
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.tmdb } returns 1
      }
      coEvery { showsDao.getById(any<String>()) } returns null
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { mediaRemoteSource.fetchShow(any<Long>()) } returns showRemote

      val show = SUT.load(MediaId.tmdb(1), false)

      assertThat(show.ids.media).isEqualTo(MediaId.tmdb(1))

      coVerifySequence {
        showsDao.getById(any<String>())
        mediaRemoteSource.fetchShow(any<Long>())
        showsDao.upsert(any())
      }
    }
  }

  @Test
  fun `Should load remote show details if cached show expired`() {
    runBlocking {
      val showDb = mockk<Show>(relaxed = true) {
        every { mediaId } returns "tmdb:1"
        every { updatedAt } returns nowUtcMillis() - TimeUnit.DAYS.toMillis(10)
      }
      val showRemote = mockk<ShowRemote>(relaxed = true) {
        every { ids?.tmdb } returns 1
      }
      coEvery { showsDao.getById(any<String>()) } returns showDb
      coEvery { showsDao.upsert(any()) } just Runs
      coEvery { mediaRemoteSource.fetchShow(any<Long>()) } returns showRemote

      val show = SUT.load(MediaId.tmdb(1), false)

      assertThat(show.ids.media).isEqualTo(MediaId.tmdb(1))

      coVerifySequence {
        showsDao.getById(any<String>())
        mediaRemoteSource.fetchShow(any<Long>())
        showsDao.upsert(any())
      }
    }
  }
}
