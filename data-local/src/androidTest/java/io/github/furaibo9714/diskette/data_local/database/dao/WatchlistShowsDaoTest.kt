@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.helpers.TestData
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistShowsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndStoreSingleEntity() {
    runBlocking {
      val show = TestData.createShow()
      val seeLaterShow = WatchlistShow.fromMediaId(show.mediaId, 999)

      database.showsDao().upsert(listOf(show))
      database.watchlistShowsDao().insert(seeLaterShow)
      val result = database.watchlistShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show.copy(updatedAt = 999, createdAt = 999)))
    }
  }

  @Test
  fun shouldInsertAndStoreSingleEntityById() {
    runBlocking {
      val show = TestData.createShow()
      val show2 = TestData.createShow().copy(mediaId = "tmdb:2")
      val seeLaterShow = WatchlistShow.fromMediaId(show.mediaId, 999)
      val seeLaterShow2 = WatchlistShow.fromMediaId(show2.mediaId, 999)

      database.showsDao().upsert(listOf(show, show2))
      database.watchlistShowsDao().insert(seeLaterShow)
      database.watchlistShowsDao().insert(seeLaterShow2)

      val result = database.watchlistShowsDao().getById("tmdb:2")
      assertThat(result).isEqualTo(show2)
    }
  }

  @Test
  fun shouldDeleteSingleEntityById() {
    runBlocking {
      val show = TestData.createShow()
      val show2 = TestData.createShow().copy(mediaId = "tmdb:2")
      val seeLaterShow = WatchlistShow.fromMediaId(show.mediaId, 999)
      val seeLaterShow2 = WatchlistShow.fromMediaId(show2.mediaId, 999)

      database.showsDao().upsert(listOf(show, show2))
      database.watchlistShowsDao().insert(seeLaterShow)
      database.watchlistShowsDao().insert(seeLaterShow2)
      database.watchlistShowsDao().deleteById("tmdb:2")

      val result = database.watchlistShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(show.copy(updatedAt = 999, createdAt = 999)))
    }
  }
}
