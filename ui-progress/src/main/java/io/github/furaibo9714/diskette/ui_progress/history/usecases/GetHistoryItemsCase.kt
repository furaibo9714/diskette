package io.github.furaibo9714.diskette.ui_progress.history.usecases

import io.github.furaibo9714.diskette.common.Config.DEFAULT_LANGUAGE
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.dateFromMillis
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toLocalZone
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.common.extensions.toUtcZone
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.removeDiacritics
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.ALL_TIME
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.LAST_30_DAYS
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.LAST_365_DAYS
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.LAST_90_DAYS
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.LAST_MONTH
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.LAST_WEEK
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.THIS_MONTH
import io.github.furaibo9714.diskette.ui_model.HistoryPeriod.THIS_WEEK
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_progress.helpers.TranslationsBundle
import io.github.furaibo9714.diskette.ui_progress.history.entities.HistoryListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import io.github.furaibo9714.diskette.ui_model.Episode as EpisodeUi

internal data class HistoryPage(
  val items: List<HistoryListItem.Episode>,
  val periodFilter: HistoryPeriod,
  val hasMore: Boolean,
)

internal class GetHistoryItemsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val mappers: Mappers,
) {

  companion object {
    const val PAGE_SIZE = 100

    /**
     * A page is capped at PAGE_SIZE episodes, so this chunking rarely kicks in today - kept as a
     * defensive bound on concurrent `async` fan-out in case a caller ever requests a larger page.
     */
    private const val LOAD_CHUNK_SIZE = 200
  }

  suspend fun loadItems(
    searchQuery: String? = "",
    offset: Int = 0,
    limit: Int = PAGE_SIZE,
  ): HistoryPage =
    withContext(dispatchers.IO) {
      val periodFilter = settingsRepository.filters.historyShowsPeriod
      val periodRange = getPeriodRange(periodFilter)

      val pageEpisodes = localSource.episodes.getAllWatchedForTrackedShowsPaged(
        fromTime = periodRange.first,
        toTime = periodRange.last,
        limit = limit,
        offset = offset,
      )

      val showIds = pageEpisodes.map { it.idShowTrakt }.distinct()

      val shows = coroutineScope {
        val async1 = async { showsRepository.myShows.loadAll() }
        val async2 = async { showsRepository.watchlistShows.loadAll() }
        awaitAll(async1, async2).flatten()
      }
      val localSeasons = if (showIds.isEmpty()) {
        emptyList()
      } else {
        localSource.seasons.getAllByShowsIds(showIds)
      }

      val language = translationsRepository.getLanguage()
      val dateFormat = dateFormatProvider.loadFullHourFormat()

      val showsById = shows.associateBy { it.traktId }
      val seasonsByShowAndNumber = localSeasons.associateBy { it.idShowTrakt to it.seasonNumber }
      val episodesByShowAndSeason = pageEpisodes.groupBy { it.idShowTrakt to it.seasonNumber }

      val items = pageEpisodes
        .chunked(LOAD_CHUNK_SIZE)
        .flatMap { chunk ->
          chunk.map { episode ->
            async {
              val show = showsById[episode.idShowTrakt]
              val season = seasonsByShowAndNumber[episode.idShowTrakt to episode.seasonNumber]

              if (show == null || season == null) {
                return@async null
              }

              val seasonEpisodes = episodesByShowAndSeason[season.idShowTrakt to season.seasonNumber].orEmpty()

              val episodeUi = mappers.episode.fromDatabase(episode)
              val seasonUi = mappers.season.fromDatabase(season, seasonEpisodes)

              HistoryListItem.Episode(
                show = show,
                season = seasonUi,
                episode = episodeUi,
                image = imagesProvider.findCachedImage(show, ImageType.POSTER),
                translations = getTranslation(language, show, episodeUi),
                dateFormat = dateFormat,
              )
            }
          }.awaitAll()
        }.filterNotNull()

      val searchItems = filterByQuery(searchQuery, dateFormat, items)

      HistoryPage(
        items = searchItems,
        periodFilter = periodFilter,
        hasMore = pageEpisodes.size == limit,
      )
    }

  private fun filterByQuery(
    query: String?,
    dateFormat: DateTimeFormatter,
    items: List<HistoryListItem.Episode>,
  ): List<HistoryListItem.Episode> {
    if (query.isNullOrBlank()) {
      return items
    }
    return items.filter {
      it.show.title
        .removeDiacritics()
        .contains(query, true) ||
        it.episode.title
          .removeDiacritics()
          .contains(query, true) ||
        it.translations
          ?.show
          ?.title
          ?.removeDiacritics()
          ?.contains(query, true) == true ||
        it.translations
          ?.episode
          ?.title
          ?.removeDiacritics()
          ?.contains(query, true) == true ||
        it.episode.lastWatchedAt
          ?.toLocalZone()
          ?.format(dateFormat)
          ?.contains(query, true) == true
    }
  }

  private suspend fun getTranslation(
    language: String,
    show: Show,
    episode: EpisodeUi,
  ): TranslationsBundle? {
    if (language == DEFAULT_LANGUAGE) {
      return null
    }
    return TranslationsBundle(
      episode = translationsRepository.loadTranslation(
        language = language,
        showId = show.ids.trakt,
        episode = episode,
        onlyLocal = true,
      ),
      show = translationsRepository.loadTranslation(
        language = language,
        show = show,
        onlyLocal = true,
      ),
    )
  }

  private fun getPeriodRange(period: HistoryPeriod): LongRange {
    val nowUtcMillis = nowUtcMillis()
    return when (period) {
      THIS_WEEK -> {
        val nowLocal = dateFromMillis(nowUtcMillis).toLocalZone()

        val weekStartLocal = nowLocal
          .minusDays(nowLocal.dayOfWeek.ordinal.toLong())
          .with(LocalTime.MIN)
        val weekStartUtc = weekStartLocal.toUtcZone().toMillis()

        val weekEndLocal = weekStartLocal.plusDays(6).with(LocalTime.MAX)
        val weekEndUtc = weekEndLocal.toUtcZone().toMillis()

        return weekStartUtc..weekEndUtc
      }
      LAST_WEEK -> {
        val now = dateFromMillis(nowUtcMillis).toLocalZone()

        val weekStartLocal = now
          .minusDays(now.dayOfWeek.ordinal.toLong())
          .minusWeeks(1)
          .with(LocalTime.MIN)
        val weekStartUtc = weekStartLocal.toUtcZone().toMillis()

        val weekEndLocal = weekStartLocal.plusDays(6).with(LocalTime.MAX)
        val weekEndUtc = weekEndLocal.toUtcZone().toMillis()

        return weekStartUtc..weekEndUtc
      }
      THIS_MONTH -> {
        val now = dateFromMillis(nowUtcMillis).toLocalZone()

        val monthStartLocal = now.with(firstDayOfMonth()).with(LocalTime.MIN)
        val monthStartUtc = monthStartLocal.toUtcZone().toMillis()

        val monthEndLocal = monthStartLocal.with(lastDayOfMonth()).with(LocalTime.MAX)
        val monthEndUtc = monthEndLocal.toUtcZone().toMillis()

        return monthStartUtc..monthEndUtc
      }
      LAST_MONTH -> {
        val now = dateFromMillis(nowUtcMillis).toLocalZone()

        val monthStartLocal = now
          .with(firstDayOfMonth())
          .minusDays(1)
          .with(firstDayOfMonth())
          .with(LocalTime.MIN)
        val monthStartUtc = monthStartLocal.toUtcZone().toMillis()

        val monthEndLocal = monthStartLocal.with(lastDayOfMonth()).with(LocalTime.MAX)
        val monthEndUtc = monthEndLocal.toUtcZone().toMillis()

        return monthStartUtc..monthEndUtc
      }
      LAST_30_DAYS -> {
        (nowUtcMillis - 30.days.inWholeMilliseconds)..nowUtcMillis
      }
      LAST_90_DAYS -> {
        (nowUtcMillis - 90.days.inWholeMilliseconds)..nowUtcMillis
      }
      LAST_365_DAYS -> {
        (nowUtcMillis - 365.days.inWholeMilliseconds)..nowUtcMillis
      }
      ALL_TIME -> {
        (nowUtcMillis - 36159.days.inWholeMilliseconds)..nowUtcMillis
      } // Limited to 99 years
    }
  }
}
