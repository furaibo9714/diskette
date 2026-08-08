package io.github.furaibo9714.diskette.repository

import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.ArchiveShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MyShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.WatchlistShowsDao
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow
import io.github.furaibo9714.diskette.repository.common.BaseMockTest
import io.github.furaibo9714.diskette.repository.shows.ShowsCollectionCache
import io.github.furaibo9714.diskette.repository.shows.WatchlistShowsRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb

class WatchlistShowsRepositoryTest : BaseMockTest() {

  @MockK lateinit var seeLaterShowsDao: WatchlistShowsDao
  @MockK lateinit var myShowsDao: MyShowsDao
  @MockK lateinit var archivedShowsDao: ArchiveShowsDao

  @RelaxedMockK lateinit var showDb: ShowDb

  private lateinit var SUT: WatchlistShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = WatchlistShowsRepository(database, transactions, mappers, ShowsCollectionCache())

    coEvery { database.watchlistShows } returns seeLaterShowsDao
    coEvery { database.myShows } returns myShowsDao
    coEvery { database.archiveShows } returns archivedShowsDao
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(seeLaterShowsDao)
  }

  @Test
  fun `Should load and map all SeeLater shows`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAll() } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      SUT.loadAll()

      coVerify(exactly = 1) { seeLaterShowsDao.getAll() }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map single SeeLater show by Trakt ID`() {
    runBlocking {
      val show = Show.EMPTY.copy(title = "Test")

      coEvery { seeLaterShowsDao.getById(any()) } returns showDb
      coEvery { mappers.show.fromDatabase(any()) } returns show

      val testShow = SUT.load(MediaId.tmdb(1))

      assertThat(testShow?.title).isEqualTo(show.title)
      coVerify(exactly = 1) { seeLaterShowsDao.getById(any()) }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should insert show into database using media id`() {
    runBlocking {
      coJustRun { myShowsDao.deleteById(any()) }
      coJustRun { archivedShowsDao.deleteById(any()) }

      val slot = slot<WatchlistShow>()
      coJustRun { seeLaterShowsDao.insert(capture(slot)) }

      SUT.insert(MediaId.tmdb(1))

      assertThat(slot.captured.id).isEqualTo(0)
      assertThat(slot.captured.mediaId).isEqualTo("tmdb:1")

      coVerify(exactly = 1) { seeLaterShowsDao.insert(any()) }
    }
  }

  @Test
  fun `Should delete show from archived and my shows when inserting into see later`() {
    runBlocking {
      coJustRun { myShowsDao.deleteById(any()) }
      coJustRun { archivedShowsDao.deleteById(any()) }

      val slot = slot<WatchlistShow>()
      coJustRun { seeLaterShowsDao.insert(capture(slot)) }

      SUT.insert(MediaId.tmdb(1))

      assertThat(slot.captured.id).isEqualTo(0)
      assertThat(slot.captured.mediaId).isEqualTo("tmdb:1")

      coVerify(exactly = 1) { seeLaterShowsDao.insert(any()) }
      coVerify(exactly = 1) { myShowsDao.deleteById("tmdb:1") }
      coVerify(exactly = 1) { archivedShowsDao.deleteById("tmdb:1") }
    }
  }

  @Test
  fun `Should delete show from database using media id`() {
    runBlocking {
      val slot = slot<String>()
      coJustRun { seeLaterShowsDao.deleteById(capture(slot)) }

      SUT.delete(MediaId.tmdb(10))

      assertThat(slot.captured).isEqualTo("tmdb:10")
      coVerify(exactly = 1) { seeLaterShowsDao.deleteById("tmdb:10") }
    }
  }

  @Test
  fun `Should load all SeeLater shows ids`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAllMediaIds() } returns listOf("tmdb:1", "tmdb:2")

      val ids = SUT.loadAllIds()

      assertThat(ids).containsExactly(MediaId.tmdb(1), MediaId.tmdb(2))
      coVerify(exactly = 1) { seeLaterShowsDao.getAllMediaIds() }
    }
  }
}
