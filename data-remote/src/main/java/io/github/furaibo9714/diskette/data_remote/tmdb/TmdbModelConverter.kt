package io.github.furaibo9714.diskette.data_remote.tmdb

import io.github.furaibo9714.diskette.data_remote.media.model.Episode
import io.github.furaibo9714.diskette.data_remote.media.model.Ids
import io.github.furaibo9714.diskette.data_remote.media.model.Movie
import io.github.furaibo9714.diskette.data_remote.media.model.MovieCollection
import io.github.furaibo9714.diskette.data_remote.media.model.PersonCredit
import io.github.furaibo9714.diskette.data_remote.media.model.SearchResult
import io.github.furaibo9714.diskette.data_remote.media.model.Season
import io.github.furaibo9714.diskette.data_remote.media.model.SeasonTranslation
import io.github.furaibo9714.diskette.data_remote.media.model.Show
import io.github.furaibo9714.diskette.data_remote.media.model.Translation
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbCollectionSummary
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbEpisodeDetails
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbMovieDetails
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPersonCreditsResponse
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSearchResultItem
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSeasonDetails
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbShowDetails
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbTranslationsResponse
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbVideosResponse

/**
 * Converts TMDB's response shapes into the internal DTOs in `data_remote.media.model`, which the
 * repository mappers and ui_model types consume. `ids.tmdb` always holds the real TMDB id.
 */
object TmdbModelConverter {

  private const val MEDIA_TYPE_MOVIE = "movie"
  private const val MEDIA_TYPE_TV = "tv"

  fun toSearchResult(
    item: TmdbSearchResultItem,
    order: Int,
  ): SearchResult? {
    val tmdbId = item.id ?: return null
    val ids = toIds(tmdbId)
    return when (item.media_type) {
      MEDIA_TYPE_MOVIE -> SearchResult(order = order, score = item.popularity, show = null, movie = toMovie(item, ids))
      MEDIA_TYPE_TV -> SearchResult(order = order, score = item.popularity, show = toShow(item, ids), movie = null)
      else -> null
    }
  }

  /** Converts a paginated list item (similar/trending/popular/discover) directly to a Show. */
  fun toShowSummary(item: TmdbSearchResultItem): Show? {
    val tmdbId = item.id ?: return null
    return toShow(item, toIds(tmdbId))
  }

  /** Converts a paginated list item (similar/trending/popular/discover) directly to a Movie. */
  fun toMovieSummary(item: TmdbSearchResultItem): Movie? {
    val tmdbId = item.id ?: return null
    return toMovie(item, toIds(tmdbId))
  }

  private fun toShow(
    item: TmdbSearchResultItem,
    ids: Ids,
  ) = Show(
    ids = ids,
    title = item.name ?: item.original_name,
    year = parseYear(item.first_air_date),
    overview = item.overview,
    first_aired = toIsoDateTime(item.first_air_date),
    runtime = null,
    airs = null,
    certification = null,
    network = null,
    country = null,
    trailer = null,
    homepage = null,
    status = null,
    rating = item.vote_average,
    votes = item.vote_count,
    comment_count = null,
    genres = TmdbGenreSlugs.tvSlugs(item.genre_ids),
    aired_episodes = null,
  )

  private fun toMovie(
    item: TmdbSearchResultItem,
    ids: Ids,
  ) = Movie(
    ids = ids,
    title = item.title ?: item.original_title,
    year = parseYear(item.release_date),
    overview = item.overview,
    released = item.release_date,
    runtime = null,
    country = null,
    trailer = null,
    homepage = null,
    status = null,
    rating = item.vote_average,
    votes = item.vote_count,
    comment_count = null,
    genres = TmdbGenreSlugs.movieSlugs(item.genre_ids),
    language = item.original_language,
  )

  private const val PREFERRED_COUNTRY = "US"

  fun toShow(
    tmdbId: Long,
    details: TmdbShowDetails,
  ) = Show(
    ids = toIds(tmdbId),
    title = details.name ?: details.original_name,
    year = parseYear(details.first_air_date),
    overview = details.overview,
    first_aired = toIsoDateTime(details.first_air_date),
    runtime = details.episode_run_time?.firstOrNull(),
    airs = null,
    certification = showCertification(details),
    network = details.networks?.firstOrNull()?.name,
    country = details.origin_country?.firstOrNull(),
    trailer = trailerUrl(details.videos),
    homepage = details.homepage,
    status = details.status?.lowercase(),
    rating = details.vote_average,
    votes = details.vote_count,
    comment_count = null,
    genres = TmdbGenreSlugs.tvSlugs(details.genres?.mapNotNull { it.id }),
    aired_episodes = details.number_of_episodes,
  )

  fun toMovie(
    tmdbId: Long,
    details: TmdbMovieDetails,
  ) = Movie(
    ids = toIds(tmdbId),
    title = details.title ?: details.original_title,
    year = parseYear(details.release_date),
    overview = details.overview,
    released = details.release_date,
    runtime = details.runtime,
    country = details.production_countries?.firstOrNull()?.iso_3166_1,
    trailer = trailerUrl(details.videos),
    homepage = details.homepage,
    status = details.status?.lowercase(),
    rating = details.vote_average,
    votes = details.vote_count,
    comment_count = null,
    genres = TmdbGenreSlugs.movieSlugs(details.genres?.mapNotNull { it.id }),
    language = details.original_language,
  )

  fun toShowTranslations(response: TmdbTranslationsResponse): List<Translation> =
    response.translations.orEmpty().map {
      Translation(
        title = it.data?.name ?: it.data?.title,
        overview = it.data?.overview,
        language = it.iso_639_1,
        country = it.iso_3166_1,
      )
    }

  fun toMovieTranslations(response: TmdbTranslationsResponse): List<Translation> =
    response.translations.orEmpty().map {
      Translation(
        title = it.data?.title,
        overview = it.data?.overview,
        language = it.iso_639_1,
        country = it.iso_3166_1,
      )
    }

  /**
   * TMDB returns a season's episodes already translated when the request carries a `language`, so
   * each episode's localised name/overview becomes one [SeasonTranslation] entry keyed by that
   * episode's ids - which is how callers match a translation back to the episode it belongs to.
   */
  fun toSeasonTranslations(
    details: TmdbSeasonDetails,
    language: String,
  ): List<SeasonTranslation> =
    details.episodes.orEmpty().mapNotNull { episode ->
      val episodeId = episode.id ?: return@mapNotNull null
      SeasonTranslation(
        season = episode.season_number ?: details.season_number ?: -1,
        number = episode.episode_number ?: -1,
        ids = toIds(episodeId),
        translations = listOf(
          Translation(
            title = episode.name,
            overview = episode.overview,
            language = language,
            country = null,
          ),
        ),
      )
    }

  fun toMovieCollection(
    collection: TmdbCollectionSummary,
  ): MovieCollection? {
    val collectionId = collection.id ?: return null
    return MovieCollection(
      ids = toIds(collectionId),
      name = collection.name ?: "",
      description = collection.overview ?: "",
      privacy = "",
      item_count = 0,
      likes = 0,
    )
  }

  fun toSeason(details: TmdbSeasonDetails): Season =
    Season(
      ids = details.id?.let { toIds(it) },
      number = details.season_number,
      episode_count = details.episodes?.size,
      aired_episodes = details.episodes?.size,
      title = details.name,
      first_aired = toIsoDateTime(details.air_date),
      overview = details.overview,
      rating = null,
      episodes = details.episodes?.map { toEpisode(it) } ?: emptyList(),
    )

  fun toEpisode(episode: TmdbEpisodeDetails): Episode =
    Episode(
      season = episode.season_number,
      number = episode.episode_number,
      title = episode.name,
      ids = episode.id?.let { toIds(it) },
      overview = episode.overview,
      rating = episode.vote_average,
      votes = episode.vote_count,
      comment_count = null,
      first_aired = toIsoDateTime(episode.air_date),
      runtime = episode.runtime,
      number_abs = null,
      last_watched_at = null,
    )

  fun toPersonShowCredits(
    response: TmdbPersonCreditsResponse,
    isCast: Boolean,
  ): List<PersonCredit> {
    val items = if (isCast) response.cast else response.crew
    return items.orEmpty().mapNotNull { item ->
      val show = toShowSummary(item) ?: return@mapNotNull null
      PersonCredit(characters = null, episode_count = null, series_regular = null, show = show, movie = null)
    }
  }

  fun toPersonMovieCredits(
    response: TmdbPersonCreditsResponse,
    isCast: Boolean,
  ): List<PersonCredit> {
    val items = if (isCast) response.cast else response.crew
    return items.orEmpty().mapNotNull { item ->
      val movie = toMovieSummary(item) ?: return@mapNotNull null
      PersonCredit(characters = null, episode_count = null, series_regular = null, show = null, movie = movie)
    }
  }

  private fun showCertification(details: TmdbShowDetails): String? {
    val results = details.content_ratings?.results.orEmpty()
    val preferred = results.find { it.iso_3166_1 == PREFERRED_COUNTRY } ?: results.firstOrNull()
    return preferred?.rating?.takeIf { it.isNotBlank() }
  }

  private fun trailerUrl(videos: TmdbVideosResponse?): String? {
    val video = videos?.results.orEmpty().let { list ->
      list.find { it.site == "YouTube" && it.type == "Trailer" && it.official == true }
        ?: list.find { it.site == "YouTube" && it.type == "Trailer" }
    }
    return video?.key?.let { "https://www.youtube.com/watch?v=$it" }
  }

  private fun toIds(tmdbId: Long) =
    Ids(
      slug = null,
      tvdb = null,
      imdb = null,
      tmdb = tmdbId,
    )

  private fun parseYear(date: String?): Int? =
    date?.takeIf { it.length >= 4 }?.substring(0, 4)?.toIntOrNull()

  /**
   * TMDB's date fields (`first_air_date`/`air_date`) are plain dates ("2008-01-20"), but Trakt's
   * equivalent fields are full ISO-8601 datetimes with an offset ("2008-01-20T02:00:00.000Z"),
   * and downstream mappers (`SeasonMapper`, `EpisodeMapper`, and callers of
   * `String?.toZonedDateTime()`) parse them with `ZonedDateTime.parse()`, which throws on a
   * plain date. Normalize by appending a midnight-UTC time component. Do NOT use this for
   * `Movie.released` - that's parsed with `LocalDate.parse()`, which wants the plain date as-is.
   */
  private fun toIsoDateTime(date: String?): String? =
    date?.takeIf { it.isNotBlank() }?.let { "${it}T00:00:00Z" }
}
