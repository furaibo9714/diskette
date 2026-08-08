@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpisodesDaoTest : BaseDaoTest() {

  @Test
  fun shouldStoreEpisodesForSeason() {
    runBlocking {
      val season = TestData.createSeason()
      val episode1 = TestData.createEpisode().copy(mediaId = "tmdb:1")
      val episode2 = TestData.createEpisode().copy(mediaId = "tmdb:2")

      database.seasonsDao().upsert(listOf(season))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllForSeason("tmdb:1")
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))
    }
  }

  @Test
  fun shouldUpdateEpisodeIfAlreadyExists() {
    runBlocking {
      val season = TestData.createSeason()
      val episode1 = TestData.createEpisode().copy(mediaId = "tmdb:1")
      val episode2 = TestData.createEpisode().copy(mediaId = "tmdb:2")

      database.seasonsDao().upsert(listOf(season))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllForSeason("tmdb:1")
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))

      val updated = episode2.copy(title = "Updated")
      database.episodesDao().upsert(listOf(episode1, updated))

      val result2 = database.episodesDao().getAllForSeason("tmdb:1")
      assertThat(result2).containsExactlyElementsIn(listOf(episode1, updated))
    }
  }

  @Test
  fun shouldStoreEpisodesForShows() {
    runBlocking {
      val show1 = TestData.createShow().copy(mediaId = "tmdb:1")
      val show2 = TestData.createShow().copy(mediaId = "tmdb:2")

      val season1 = TestData.createSeason().copy(showMediaId = show1.mediaId)
      val season2 = TestData.createSeason().copy(showMediaId = show2.mediaId)

      val episode1 = TestData.createEpisode().copy(
        mediaId = "tmdb:1",
        showMediaId = show1.mediaId,
        idSeason = season1.mediaId,
      )
      val episode2 = TestData.createEpisode().copy(
        mediaId = "tmdb:2",
        showMediaId = show2.mediaId,
        idSeason = season2.mediaId,
      )

      database.showsDao().upsert(listOf(show1, show2))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result2 = database.episodesDao().getAllByShowId("tmdb:2")
      assertThat(result2).containsExactlyElementsIn(listOf(episode2))
    }
  }

  @Test
  fun shouldReturnWatchedIdsForShow() {
    runBlocking {
      val show = TestData.createShow().copy(mediaId = "tmdb:1")

      val season1 = TestData.createSeason().copy(showMediaId = show.mediaId)
      val season2 = TestData.createSeason().copy(showMediaId = show.mediaId)

      val episode1 = TestData.createEpisode().copy(
        mediaId = "tmdb:1",
        showMediaId = show.mediaId,
        idSeason = season1.mediaId,
        isWatched = true,
      )
      val episode2 = TestData.createEpisode().copy(
        mediaId = "tmdb:2",
        showMediaId = show.mediaId,
        idSeason = season2.mediaId,
        isWatched = false,
      )

      database.showsDao().upsert(listOf(show))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllWatchedIdsForShows(listOf(show.mediaId))
      assertThat(result).containsExactlyElementsIn(listOf(episode1.mediaId))
    }
  }

  @Test
  fun shouldDeleteAllUnwatchedForShow() {
    runBlocking {
      val show = TestData.createShow().copy(mediaId = "tmdb:1")

      val season1 = TestData.createSeason().copy(showMediaId = show.mediaId)
      val season2 = TestData.createSeason().copy(showMediaId = show.mediaId)

      val episode1 = TestData.createEpisode().copy(
        mediaId = "tmdb:1",
        showMediaId = show.mediaId,
        idSeason = season1.mediaId,
        isWatched = true,
      )
      val episode2 = TestData.createEpisode().copy(
        mediaId = "tmdb:2",
        showMediaId = show.mediaId,
        idSeason = season2.mediaId,
        isWatched = false,
      )

      database.showsDao().upsert(listOf(show))
      database.seasonsDao().upsert(listOf(season1, season2))
      database.episodesDao().upsert(listOf(episode1, episode2))

      val result = database.episodesDao().getAllByShowId(show.mediaId)
      assertThat(result).containsExactlyElementsIn(listOf(episode1, episode2))

      database.episodesDao().deleteAllUnwatchedForShow(show.mediaId)

      val result2 = database.episodesDao().getAllByShowId(show.mediaId)
      assertThat(result2).containsExactlyElementsIn(listOf(episode1))
    }
  }
}
