@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.helpers.TestData
import io.github.furaibo9714.diskette.data_local.database.model.MyShow
import io.github.furaibo9714.diskette.data_local.database.model.Show
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyShowsDaoTest : BaseDaoTest() {

  private val shows = mutableListOf<Show>()

  @Before
  fun setUp() =
    runBlocking {
      shows.add(TestData.createShow().copy(mediaId = "tmdb:1"))
      shows.add(TestData.createShow().copy(mediaId = "tmdb:2"))
      shows.add(TestData.createShow().copy(mediaId = "tmdb:3"))

      database.showsDao().upsert(shows)
    }

  @Test
  fun shouldInsertAndStoreEntities() {
    runBlocking {
      val myShow = MyShow.fromMediaId(shows[0].mediaId, 0, 0, 0)

      database.myShowsDao().insert(listOf(myShow))

      val result = database.myShowsDao().getAll()
      assertThat(result).containsExactlyElementsIn(listOf(shows[0]))
    }
  }

  @Test
  fun shouldReturnIdsOnly() {
    runBlocking {
      val myShow1 = MyShow.fromMediaId(shows[0].mediaId, 0, 0, 0)
      val myShow2 = MyShow.fromMediaId(shows[1].mediaId, 0, 0, 0)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getAllMediaIds()
      assertThat(result).containsExactlyElementsIn(listOf(shows[0].mediaId, shows[1].mediaId))
    }
  }

  @Test
  fun shouldReturnMostRecentAddedShows() {
    runBlocking {
      val myShow1 = MyShow.fromMediaId(shows[0].mediaId, 0, 0, 0)
      val myShow2 = MyShow.fromMediaId(shows[1].mediaId, 999, 999, 999)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getAllRecent(10)
      assertThat(result[0]).isEqualTo(shows[1])
      assertThat(result[1]).isEqualTo(shows[0])
    }
  }

  @Test
  fun shouldReturnById() {
    runBlocking {
      val myShow1 = MyShow.fromMediaId(shows[0].mediaId, 0, 0, 0)
      val myShow2 = MyShow.fromMediaId(shows[1].mediaId, 0, 0, 0)

      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().insert(listOf(myShow2))

      val result = database.myShowsDao().getById(shows[1].mediaId)
      assertThat(result).isEqualTo(shows[1])
    }
  }

  @Test
  fun shouldDeleteByIdWithoutDeletingParent() {
    runBlocking {
      val myShow1 = MyShow.fromMediaId(shows[1].mediaId, 0, 0, 0)

      val showsSize = shows.size
      database.myShowsDao().insert(listOf(myShow1))
      database.myShowsDao().deleteById(shows[1].mediaId)
      val result = database.myShowsDao().getById(shows[1].mediaId)

      assertThat(result).isNull()
      assertThat(shows).hasSize(showsSize)
    }
  }
}
