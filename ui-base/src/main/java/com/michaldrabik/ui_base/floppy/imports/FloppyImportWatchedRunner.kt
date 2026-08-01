package com.michaldrabik.ui_base.floppy.imports

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import com.michaldrabik.data_remote.floppy.api.FloppyService
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.repository.movies.MyMoviesRepository
import com.michaldrabik.ui_model.IdTrakt
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls the user's watched movies/episodes from Floppy and reconciles Showly's local watched
 * state. Only items already known locally can be reconciled: movies matched by tmdb id, episodes
 * matched by tmdb id + season/episode number against a locally-fetched season. Floppy carries no
 * Trakt id and no delta/"since" endpoint, so this is a full reconcile each run.
 */
@Singleton
class FloppyImportWatchedRunner @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val myMoviesRepository: MyMoviesRepository,
  private val episodesManager: EpisodesManager,
) {

  suspend fun run(): Int {
    if (!connectionManager.isConfigured()) return 0

    val service = connectionManager.service()
    var count = 0
    count += importWatchedMovies(service)
    count += importWatchedEpisodes(service)
    return count
  }

  private suspend fun importWatchedMovies(service: FloppyService): Int {
    var imported = 0
    var offset = 0
    while (true) {
      val page = service.getTrackedMedia(MEDIA_TYPE_MOVIE, FloppyService.MEDIA_LIST_PAGE_SIZE, offset)
      page.results
        .filter { (it.status ?: 0) >= FLOPPY_STATUS_COMPLETED }
        .forEach { media ->
          val tmdbId = media.item.mediaId.toLongOrNull() ?: return@forEach
          val movie = localSource.movies.getByTmdbId(tmdbId) ?: return@forEach
          val id = IdTrakt(movie.idTrakt)
          if (!myMoviesRepository.exists(id)) {
            myMoviesRepository.insert(id, customDate = null)
            imported++
          }
        }
      if (page.results.size < FloppyService.MEDIA_LIST_PAGE_SIZE) break
      offset += FloppyService.MEDIA_LIST_PAGE_SIZE
    }
    Timber.d("Imported $imported watched movie(s) from Floppy.")
    return imported
  }

  private suspend fun importWatchedEpisodes(service: FloppyService): Int {
    var imported = 0
    var offset = 0
    while (true) {
      val page = service.getTrackedMedia(MEDIA_TYPE_EPISODE, FloppyService.MEDIA_LIST_PAGE_SIZE, offset)
      page.results
        .filter { (it.status ?: 0) >= FLOPPY_STATUS_COMPLETED }
        .forEach { media ->
          val tmdbId = media.item.mediaId.toLongOrNull() ?: return@forEach
          val seasonNumber = media.item.seasonNumber ?: return@forEach
          val episodeNumber = media.item.episodeNumber ?: return@forEach
          val show = localSource.shows.getByTmdbId(tmdbId) ?: return@forEach
          val episode = localSource.episodes
            .getAllByShowId(show.idTrakt, seasonNumber)
            .find { it.episodeNumber == episodeNumber }
            ?: return@forEach
          if (!episode.isWatched) {
            episodesManager.setEpisodeWatched(episode.idTrakt, episode.idSeason, IdTrakt(show.idTrakt), customDate = null)
            imported++
          }
        }
      if (page.results.size < FloppyService.MEDIA_LIST_PAGE_SIZE) break
      offset += FloppyService.MEDIA_LIST_PAGE_SIZE
    }
    Timber.d("Imported $imported watched episode(s) from Floppy.")
    return imported
  }

  companion object {
    private const val MEDIA_TYPE_EPISODE = "episode"
    private const val FLOPPY_STATUS_COMPLETED = 3
  }
}
