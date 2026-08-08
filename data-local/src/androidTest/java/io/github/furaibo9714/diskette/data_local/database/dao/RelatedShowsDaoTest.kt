@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.helpers.TestData
import io.github.furaibo9714.diskette.data_local.database.model.RelatedShow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RelatedShowsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndDeleteSingleEntity() {
    runBlocking {
      val show = TestData.createShow().copy(mediaId = "tmdb:99")
      val relatedShow = RelatedShow(1, "tmdb:1", "tmdb:99", 999)

      database.showsDao().upsert(listOf(show))
      database.relatedShowsDao().insert(listOf(relatedShow))
      val result = database.relatedShowsDao().getAll()
      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(relatedShow)

      database.relatedShowsDao().deleteById("tmdb:99")
      val result2 = database.relatedShowsDao().getAll()
      assertThat(result2).isEmpty()
      assertThat(database.showsDao().getById("tmdb:99")).isEqualTo(show)
    }
  }

  @Test
  fun shouldReturnRelatedShowsForId() {
    runBlocking {
      val show1 = TestData.createShow().copy(mediaId = "tmdb:1", updatedAt = 100)
      val show2 = TestData.createShow().copy(mediaId = "tmdb:2", updatedAt = 100)
      val show3 = TestData.createShow().copy(mediaId = "tmdb:3", updatedAt = 100)

      val relatedShow1 = RelatedShow(1, "tmdb:2", "tmdb:1", 200)
      val relatedShow2 = RelatedShow(2, "tmdb:3", "tmdb:1", 200)
      val relatedShow3 = RelatedShow(3, "tmdb:1", "tmdb:2", 200)

      database.showsDao().upsert(listOf(show1, show2, show3))
      database.relatedShowsDao().insert(listOf(relatedShow1, relatedShow2, relatedShow3))
      val result = database.relatedShowsDao().getAllById("tmdb:1")
      assertThat(result).containsExactlyElementsIn(listOf(relatedShow1, relatedShow2))
    }
  }
}
