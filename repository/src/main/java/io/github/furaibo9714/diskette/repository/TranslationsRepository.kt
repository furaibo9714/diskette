package io.github.furaibo9714.diskette.repository

import android.content.SharedPreferences
import io.github.furaibo9714.diskette.common.Config.DEFAULT_LANGUAGE
import io.github.furaibo9714.diskette.common.ConfigVariant
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.EpisodeTranslation
import io.github.furaibo9714.diskette.data_local.database.model.MovieTranslation
import io.github.furaibo9714.diskette.data_local.database.model.ShowTranslation
import io.github.furaibo9714.diskette.data_local.database.model.TranslationsMoviesSyncLog
import io.github.furaibo9714.diskette.data_local.database.model.TranslationsSyncLog
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.data_remote.media.model.Translation as TranslationRemote
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository.Key.LANGUAGE
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.SeasonTranslation
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TranslationsRepository @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  fun getLanguage() = miscPreferences.getString(LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

  suspend fun loadAllShowsLocal(language: String = DEFAULT_LANGUAGE): Map<Long, Translation> {
    val local = localSource.showTranslations.getAll(language)
    return local.associate {
      Pair(it.mediaId, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadAllMoviesLocal(language: String = DEFAULT_LANGUAGE): Map<Long, Translation> {
    val local = localSource.movieTranslations.getAll(language)
    return local.associate {
      Pair(it.mediaId, mappers.translation.fromDatabase(it))
    }
  }

  suspend fun loadTranslation(
    show: Show,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val local = localSource.showTranslations.getById(show.mediaId.key, language)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = localSource.translationsShowsSyncLog.getById(show.mediaId.key)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      remoteSource.media
        .fetchShowTranslations(show.mediaId, language, show.ids.tmdb.id)
        .firstOrNull { chineseLanguagePredicate(it) && frenchLanguagePredicate(it) }
    } catch (error: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = ShowTranslation.fromMediaId(
      show.mediaId.key,
      translation.title,
      language,
      translation.overview,
      nowUtcMillis(),
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      localSource.showTranslations.insertSingle(translationDb)
    }
    localSource.translationsShowsSyncLog.upsert(TranslationsSyncLog(show.mediaId.key, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    movie: Movie,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val local = localSource.movieTranslations.getById(movie.mediaId.key, language)
    local?.let {
      return mappers.translation.fromDatabase(it)
    }
    if (onlyLocal) return null

    val timestamp = localSource.translationsMoviesSyncLog.getById(movie.mediaId.key)?.syncedAt ?: 0
    if (nowUtcMillis() - timestamp < ConfigVariant.TRANSLATION_SYNC_SHOW_MOVIE_COOLDOWN) {
      return Translation.EMPTY
    }

    val remoteTranslation = try {
      remoteSource.media
        .fetchMovieTranslations(movie.mediaId, language, movie.ids.tmdb.id)
        .firstOrNull { chineseLanguagePredicate(it) && frenchLanguagePredicate(it) }
    } catch (error: Throwable) {
      null
    }

    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = MovieTranslation.fromMediaId(
      movie.mediaId.key,
      translation.title,
      language,
      translation.overview,
      nowUtcMillis(),
    )

    if (translationDb.overview.isNotBlank() || translationDb.title.isNotBlank()) {
      localSource.movieTranslations.insertSingle(translationDb)
    }
    localSource.translationsMoviesSyncLog.upsert(TranslationsMoviesSyncLog(movie.mediaId.key, nowUtcMillis()))

    return translation
  }

  suspend fun loadTranslation(
    episode: Episode,
    showId: MediaId,
    language: String = DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false,
  ): Translation? {
    val nowMillis = nowUtcMillis()
    val local = localSource.episodesTranslations.getById(episode.ids.media.id, showId.id, language)
    local?.let {
      val isCacheValid = nowMillis - it.updatedAt < ConfigVariant.TRANSLATION_SYNC_EPISODE_COOLDOWN
      if (it.title.isNotBlank() && it.overview.isNotBlank()) {
        return mappers.translation.fromDatabase(it)
      }
      if ((it.title.isNotBlank() || it.overview.isNotBlank()) && (isCacheValid || onlyLocal)) {
        return mappers.translation.fromDatabase(it)
      }
    }

    if (onlyLocal) return null

    val remoteTranslations = remoteSource.media
      .fetchSeasonTranslations(showId.id, episode.season, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslations
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromMediaId(
          traktEpisodeId = item.ids.media.id,
          traktShowId = showId.id,
          title = item.title,
          overview = item.overview,
          language = language,
          createdAt = nowMillis,
        )
        localSource.episodesTranslations.insertSingle(dbItem)
      }

    remoteTranslations
      .find { it.ids.media == episode.ids.media }
      ?.let {
        return Translation(it.title, it.overview, it.language)
      }

    return null
  }

  suspend fun loadTranslations(
    season: Season,
    showId: MediaId,
    language: String = DEFAULT_LANGUAGE,
  ): List<SeasonTranslation> {
    val episodes = season.episodes.toList()
    val episodesIds = season.episodes.map { it.ids.media.id }

    val local = localSource.episodesTranslations.getByIds(episodesIds, showId.id, language)
    val hasAllTranslated = local.isNotEmpty() && local.all { it.title.isNotBlank() && it.overview.isNotBlank() }
    val isCacheValid =
      local.isNotEmpty() && nowUtcMillis() - local.first().updatedAt < ConfigVariant.TRANSLATION_SYNC_EPISODE_COOLDOWN

    if (hasAllTranslated || (!hasAllTranslated && isCacheValid)) {
      return episodes.map { episode ->
        val translation = local.find { it.mediaId == episode.ids.media.id }
        SeasonTranslation(
          ids = episode.ids.copy(),
          title = translation?.title ?: "",
          overview = translation?.overview ?: "",
          seasonNumber = season.number,
          episodeNumber = episode.number,
          language = language,
          isLocal = true,
        )
      }
    }

    val remoteTranslation = remoteSource.media
      .fetchSeasonTranslations(showId.id, season.number, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslation
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromMediaId(
          item.ids.media.id,
          showId.id,
          item.title,
          language,
          item.overview,
          nowUtcMillis(),
        )
        localSource.episodesTranslations.insertSingle(dbItem)
      }

    return episodes.map { episode ->
      val translation = remoteTranslation.find { it.ids.media.id == episode.ids.media.id }
      SeasonTranslation(
        ids = episode.ids.copy(),
        title = translation?.title ?: "",
        overview = translation?.overview ?: "",
        seasonNumber = season.number,
        episodeNumber = episode.number,
        language = language,
        isLocal = true,
      )
    }
  }

  private fun chineseLanguagePredicate(translation: TranslationRemote) =
    if (translation.language?.lowercase() != "zh") {
      true
    } else {
      translation.country?.equals("cn", ignoreCase = true) == true
    }

  private fun frenchLanguagePredicate(translation: TranslationRemote) =
    if (translation.language?.lowercase() != "fr") {
      true
    } else {
      translation.country?.equals("fr", ignoreCase = true) == true
    }
}
