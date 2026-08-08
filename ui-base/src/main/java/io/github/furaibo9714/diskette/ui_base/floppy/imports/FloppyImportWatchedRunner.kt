package io.github.furaibo9714.diskette.ui_base.floppy.imports

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue.Companion.MEDIA_TYPE_MOVIE
import io.github.furaibo9714.diskette.data_remote.floppy.api.FloppyService
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.repository.movies.MyMoviesRepository
import io.github.furaibo9714.diskette.repository.shows.MyShowsRepository
import io.github.furaibo9714.diskette.ui_base.events.EventsManager
import io.github.furaibo9714.diskette.ui_base.events.FloppySyncProgress
import io.github.furaibo9714.diskette.ui_model.MediaId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import io.github.furaibo9714.diskette.data_local.database.model.Episode as EpisodeDb
import io.github.furaibo9714.diskette.data_local.database.model.Season as SeasonDb
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb

/**
 * Pulls the user's watched movies/episodes from Floppy and reconciles Diskette's local watched
 * state. Movies are matched/created via [FloppyManualMediaResolver] like the watchlist import.
 * Episodes are matched by show (same resolver) + season/episode number, with a thin
 * season/episode row created on first encounter when not already cached locally. Floppy carries no
 * delta/"since" endpoint, so this is a full reconcile each run.
 *
 * Emits [FloppySyncProgress] once per fetched page (rather than per item) so screens can reload
 * incrementally during a full sync without hammering the event bus on large libraries - throttled
 * to at most once every [PROGRESS_THROTTLE_MS] for the same reason as
 * [io.github.furaibo9714.diskette.ui_base.floppy.imports.FloppyImportWatchlistRunner]: on a large library each
 * reload it triggers is itself expensive, and firing on every page during a many-page reconcile
 * was observed to compound into an OOM.
 */
@Singleton
class FloppyImportWatchedRunner @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val localSource: LocalDataSource,
  private val mediaResolver: FloppyManualMediaResolver,
  private val myMoviesRepository: MyMoviesRepository,
  private val myShowsRepository: MyShowsRepository,
  private val eventsManager: EventsManager,
) {

  private var lastProgressEventAt = 0L

  /**
   * Reports (count/total) for the sub-collection currently being imported (movies, then episodes),
   * resetting at each sub-collection boundary. Fires once per fetched page - a much cheaper signal
   * than [FloppySyncProgress] below (only updates the sync notification/status text, no screen
   * reload), so unlike that event it doesn't need throttling. Deliberately not per-item: items here
   * are pure local DB writes with no network delay, so a whole page (up to 100 items) finishes
   * faster than the UI can render intermediate values - per-item updates just flash by unreadably,
   * and slowing down real import work purely to animate a counter isn't worth the tradeoff.
   */
  var progressListener: (suspend (count: Int, total: Int) -> Unit)? = null

  private suspend fun emitProgressThrottled() {
    val now = System.currentTimeMillis()
    if (now - lastProgressEventAt >= PROGRESS_THROTTLE_MS) {
      lastProgressEventAt = now
      eventsManager.sendEvent(FloppySyncProgress)
    }
  }

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
      progressListener?.invoke(offset + page.results.size, page.pagination.total)
      var pageImported = 0
      page.results
        .filter { (it.status ?: 0) >= FLOPPY_STATUS_COMPLETED }
        .forEach { media ->
          val id = mediaResolver.resolveMovieId(media.item) ?: return@forEach
          if (!myMoviesRepository.exists(id)) {
            myMoviesRepository.insert(id, customDate = null)
            pageImported++
          }
        }
      imported += pageImported
      if (pageImported > 0) emitProgressThrottled()
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
      progressListener?.invoke(offset + page.results.size, page.pagination.total)
      var pageImported = 0
      page.results
        .filter { (it.status ?: 0) >= FLOPPY_STATUS_COMPLETED }
        .forEach { media ->
          val seasonNumber = media.item.seasonNumber ?: return@forEach
          val episodeNumber = media.item.episodeNumber ?: return@forEach
          val showId = mediaResolver.resolveShowId(media.item) ?: return@forEach
          val show = localSource.shows.getById(showId.key) ?: return@forEach
          val episode = resolveEpisode(show, seasonNumber, episodeNumber)
          if (!episode.isWatched) {
            markEpisodeWatched(show, episode)
            pageImported++
          }
        }
      imported += pageImported
      if (pageImported > 0) emitProgressThrottled()
      if (page.results.size < FloppyService.MEDIA_LIST_PAGE_SIZE) break
      offset += FloppyService.MEDIA_LIST_PAGE_SIZE
    }
    Timber.d("Imported $imported watched episode(s) from Floppy.")
    return imported
  }

  private suspend fun resolveEpisode(
    show: ShowDb,
    seasonNumber: Int,
    episodeNumber: Int,
  ): EpisodeDb {
    val seasonId = localSeasonId(show.mediaId, seasonNumber)
    if (localSource.seasons.getById(seasonId) == null) {
      localSource.seasons.upsert(listOf(buildThinSeason(seasonId, show.mediaId, seasonNumber)))
    }

    val episodeId = localEpisodeId(show.mediaId, seasonNumber, episodeNumber)
    localSource.episodes.getById(show.mediaId, episodeId)?.let { return it }

    val thinEpisode = buildThinEpisode(episodeId, seasonId, show, seasonNumber, episodeNumber)
    localSource.episodes.upsert(listOf(thinEpisode))
    return thinEpisode
  }

  /**
   * Floppy names a watched episode by (show, season number, episode number) and carries no
   * provider-level episode id, so a row imported from Floppy has to name itself. These keys are
   * deterministic and scoped to the show, which is all the identity a thin row needs:
   * `EpisodesManager.invalidateSeasons` reconciles local episodes against freshly-fetched ones by
   * (season number, episode number) before falling back to id, so the real fetch's row replaces
   * this one on the show's next season refresh and carries the watched flag over.
   */
  private fun localSeasonId(
    showMediaId: String,
    seasonNumber: Int,
  ) = MediaId.local("$showMediaId/s$seasonNumber").key

  private fun localEpisodeId(
    showMediaId: String,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = MediaId.local("$showMediaId/s$seasonNumber/e$episodeNumber").key

  private suspend fun markEpisodeWatched(
    show: ShowDb,
    episode: EpisodeDb,
  ) {
    val date = nowUtc()
    localSource.episodes.upsert(listOf(episode.copy(isWatched = true, lastWatchedAt = date)))

    val showId = MediaId.parse(show.mediaId)
    if (myShowsRepository.exists(showId)) {
      myShowsRepository.updateWatchedAt(showId, date.toMillis())
    } else {
      myShowsRepository.insert(showId, date.toMillis())
    }
  }

  private fun buildThinSeason(
    seasonId: String,
    showMediaId: String,
    seasonNumber: Int,
  ) = SeasonDb(
    mediaId = seasonId,
    showMediaId = showMediaId,
    seasonNumber = seasonNumber,
    seasonTitle = "",
    seasonOverview = "",
    seasonFirstAired = null,
    episodesCount = 0,
    episodesAiredCount = 0,
    rating = null,
    isWatched = false,
  )

  private fun buildThinEpisode(
    episodeId: String,
    seasonId: String,
    show: ShowDb,
    seasonNumber: Int,
    episodeNumber: Int,
  ) = EpisodeDb(
    mediaId = episodeId,
    idSeason = seasonId,
    showMediaId = show.mediaId,
    idShowTvdb = show.idTvdb,
    idShowImdb = show.idImdb,
    idShowTmdb = show.idTmdb,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    episodeNumberAbs = null,
    episodeOverview = "",
    title = "",
    firstAired = null,
    commentsCount = 0,
    rating = 0F,
    runtime = -1,
    votesCount = 0,
    isWatched = false,
    lastExportedAt = null,
    lastWatchedAt = null,
  )

  companion object {
    private const val MEDIA_TYPE_EPISODE = "episode"
    private const val FLOPPY_STATUS_COMPLETED = 3
    private const val PROGRESS_THROTTLE_MS = 5_000L
  }
}
