package io.github.furaibo9714.diskette.data_remote.trakt

import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbGenreSlugs
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbSyntheticIds
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbToTraktModelConverter
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbMoviesService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbSearchService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbShowsService
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPerson
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSearchMultiResponse
import io.github.furaibo9714.diskette.data_remote.trakt.model.Movie
import io.github.furaibo9714.diskette.data_remote.trakt.model.PersonCredit
import io.github.furaibo9714.diskette.data_remote.trakt.model.SearchResult
import io.github.furaibo9714.diskette.data_remote.trakt.model.Season
import io.github.furaibo9714.diskette.data_remote.trakt.model.Show
import io.github.furaibo9714.diskette.data_remote.trakt.model.Translation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

/**
 * Decorates the existing (now-paywalled) [TraktRemoteDataSource] with TMDB-backed implementations
 * of the metadata/search/discovery methods, via Kotlin interface delegation. Every method NOT
 * overridden here (comments, public collections, OAuth, slug/id lookups, etc.) is forwarded to
 * [legacyTrakt] unchanged - compiler-enforced "not in scope" rather than a hand-maintained list.
 */
internal class TmdbBackedTraktRemoteDataSource(
  private val legacyTrakt: TraktRemoteDataSource,
  private val tmdbSearch: TmdbSearchService,
  private val tmdbShows: TmdbShowsService,
  private val tmdbMovies: TmdbMoviesService,
  private val tmdbPeople: TmdbService,
) : TraktRemoteDataSource by legacyTrakt {

  companion object {
    private const val RELATED_PAGES = 2
    private const val PAGE_SIZE = 20
    private const val MAX_PAGES = 5
  }

  override suspend fun fetchSearch(
    query: String,
    withMovies: Boolean,
  ): List<SearchResult> {
    val response = tmdbSearch.fetchSearchMulti(query)
    val items = response.results.orEmpty().filter {
      it.media_type == "tv" || (withMovies && it.media_type == "movie")
    }
    return items.mapIndexedNotNull { index, item ->
      TmdbToTraktModelConverter.toSearchResult(item, order = index + 1)
    }
  }

  override suspend fun fetchShow(
    traktId: Long,
    tmdbId: Long?,
  ): Show {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId) ?: return legacyTrakt.fetchShow(traktId)
    val details = tmdbShows.fetchShowDetails(resolvedTmdbId)
    val show = TmdbToTraktModelConverter.toShow(resolvedTmdbId, details)
    if (!details.episode_run_time.isNullOrEmpty()) return show

    val seasonNumbers = details.seasons.orEmpty().mapNotNull { it.season_number }
    val episodeRuntimes = coroutineScope {
      seasonNumbers
        .map { seasonNumber -> async { tmdbShows.fetchSeasonDetails(resolvedTmdbId, seasonNumber) } }
        .awaitAll()
        .flatMap { it.episodes.orEmpty() }
        .mapNotNull { it.runtime }
    }
    val runtime = episodeRuntimes.minOrNull() ?: return show
    val runtimeMax = episodeRuntimes.maxOrNull()?.takeIf { it != runtime }
    return show.copy(runtime = runtime, runtime_max = runtimeMax)
  }

  override suspend fun fetchMovie(
    traktId: Long,
    tmdbId: Long?,
  ): Movie {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId) ?: return legacyTrakt.fetchMovie(traktId)
    val details = tmdbMovies.fetchMovieDetails(resolvedTmdbId)
    return TmdbToTraktModelConverter.toMovie(resolvedTmdbId, details)
  }

  override suspend fun fetchShowTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long?,
  ): List<Translation> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchShowTranslations(traktId, code)
    val response = tmdbShows.fetchShowTranslations(resolvedTmdbId)
    return TmdbToTraktModelConverter.toShowTranslations(response).filter { it.language == code }
  }

  override suspend fun fetchMovieTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long?,
  ): List<Translation> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchMovieTranslations(traktId, code)
    val response = tmdbMovies.fetchMovieTranslations(resolvedTmdbId)
    return TmdbToTraktModelConverter.toMovieTranslations(response).filter { it.language == code }
  }

  override suspend fun fetchSeasons(
    traktId: Long,
    tmdbId: Long?,
  ): List<Season> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId) ?: return legacyTrakt.fetchSeasons(traktId)
    val showDetails = tmdbShows.fetchShowDetails(resolvedTmdbId)
    val seasonNumbers = showDetails.seasons.orEmpty().mapNotNull { it.season_number }
    return coroutineScope {
      seasonNumbers
        .map { seasonNumber -> async { tmdbShows.fetchSeasonDetails(resolvedTmdbId, seasonNumber) } }
        .awaitAll()
        .map { TmdbToTraktModelConverter.toSeason(it) }
        .sortedByDescending { it.number }
    }
  }

  override suspend fun fetchRelatedShows(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ): List<Show> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchRelatedShows(traktId, addToLimit)
    return fetchPages(RELATED_PAGES) { page -> tmdbShows.fetchSimilarShows(resolvedTmdbId, page) }
      .mapNotNull { TmdbToTraktModelConverter.toShowSummary(it) }
  }

  override suspend fun fetchRelatedMovies(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ): List<Movie> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchRelatedMovies(traktId, addToLimit)
    return fetchPages(RELATED_PAGES) { page -> tmdbMovies.fetchSimilarMovies(resolvedTmdbId, page) }
      .mapNotNull { TmdbToTraktModelConverter.toMovieSummary(it) }
  }

  override suspend fun fetchPersonShowsCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchPersonShowsCredits(traktId, type)
    val response = tmdbPeople.fetchPersonTvCredits(resolvedTmdbId)
    return TmdbToTraktModelConverter.toPersonShowCredits(response, isCast = type == TmdbPerson.Type.CAST)
  }

  override suspend fun fetchPersonMoviesCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val resolvedTmdbId = resolveTmdbId(traktId, tmdbId)
      ?: return legacyTrakt.fetchPersonMoviesCredits(traktId, type)
    val response = tmdbPeople.fetchPersonMovieCredits(resolvedTmdbId)
    return TmdbToTraktModelConverter.toPersonMovieCredits(response, isCast = type == TmdbPerson.Type.CAST)
  }

  /**
   * TMDB's `trending`/`popular` endpoints don't support server-side genre filtering, so genres are
   * filtered client-side after conversion. `discover` supports `with_genres` server-side.
   * Network filtering (Trakt's `networks` param) has no TMDB equivalent mapping and is dropped -
   * Diskette's `Network` enum encodes broadcaster names as free-text aliases with no shortcut
   * translation to TMDB's numeric `with_networks` ids.
   */
  override suspend fun fetchTrendingShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> =
    fetchPages(pagesFor(limit)) { page -> tmdbShows.fetchTrendingShows(page = page) }
      .mapNotNull { TmdbToTraktModelConverter.toShowSummary(it) }
      .filterShowsByGenres(genres)

  override suspend fun fetchPopularShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> =
    fetchPages(pagesFor(limit)) { page -> tmdbShows.fetchPopularShows(page = page) }
      .mapNotNull { TmdbToTraktModelConverter.toShowSummary(it) }
      .filterShowsByGenres(genres)

  override suspend fun fetchAnticipatedShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> {
    val genreIds = TmdbGenreSlugs.tvGenreIds(genres.toSlugList()).joinToString(",").ifBlank { null }
    return fetchPages(pagesFor(limit)) { page ->
      tmdbShows.discoverShows(page = page, withGenres = genreIds, firstAirDateFrom = today())
    }.mapNotNull { TmdbToTraktModelConverter.toShowSummary(it) }
  }

  override suspend fun fetchTrendingMovies(
    genres: String,
    limit: Int,
  ): List<Movie> =
    fetchPages(pagesFor(limit)) { page -> tmdbMovies.fetchTrendingMovies(page = page) }
      .mapNotNull { TmdbToTraktModelConverter.toMovieSummary(it) }
      .filterMoviesByGenres(genres)

  override suspend fun fetchPopularMovies(
    genres: String,
    limit: Int,
  ): List<Movie> =
    fetchPages(pagesFor(limit)) { page -> tmdbMovies.fetchPopularMovies(page = page) }
      .mapNotNull { TmdbToTraktModelConverter.toMovieSummary(it) }
      .filterMoviesByGenres(genres)

  override suspend fun fetchAnticipatedMovies(
    genres: String,
    limit: Int,
  ): List<Movie> {
    val genreIds = TmdbGenreSlugs.movieGenreIds(genres.toSlugList()).joinToString(",").ifBlank { null }
    return fetchPages(pagesFor(limit)) { page ->
      tmdbMovies.discoverMovies(page = page, withGenres = genreIds, releaseDateFrom = today())
    }.mapNotNull { TmdbToTraktModelConverter.toMovieSummary(it) }
  }

  private fun List<Show>.filterShowsByGenres(genres: String): List<Show> {
    val slugs = genres.toSlugList()
    if (slugs.isEmpty()) return this
    return filter { it.genres.orEmpty().any { genre -> genre in slugs } }
  }

  private fun List<Movie>.filterMoviesByGenres(genres: String): List<Movie> {
    val slugs = genres.toSlugList()
    if (slugs.isEmpty()) return this
    return filter { it.genres.orEmpty().any { genre -> genre in slugs } }
  }

  private fun String.toSlugList() = split(",").map { it.trim() }.filter { it.isNotEmpty() }

  private fun today(): String = LocalDate.now().toString()

  private fun pagesFor(limit: Int): Int = (limit / PAGE_SIZE).coerceIn(1, MAX_PAGES)

  /** Fetches [pages] pages of a paginated TMDB list endpoint concurrently and flattens the results. */
  private suspend fun fetchPages(
    pages: Int,
    call: suspend (page: Int) -> TmdbSearchMultiResponse,
  ) = coroutineScope {
    (1..pages)
      .map { page -> async { call(page) } }
      .awaitAll()
      .flatMap { it.results.orEmpty() }
  }

  /**
   * Freshly TMDB-sourced content carries a synthetic trakt id (see [TmdbSyntheticIds]),
   * trivially reversible. Content already cached locally before this migration carries a real
   * legacy Trakt id with no way to resolve a TMDB id from it - callers pass the TMDB id they
   * already have on hand (from the local DB row) for that case. If neither is available, there's
   * nothing to resolve, and the caller falls back to the old (paywalled) Trakt call, degrading to
   * today's status quo rather than crashing a new code path.
   */
  private fun resolveTmdbId(
    traktId: Long,
    tmdbId: Long?,
  ): Long? {
    if (tmdbId != null && tmdbId > 0) return tmdbId
    if (TmdbSyntheticIds.isSynthetic(traktId)) return TmdbSyntheticIds.toTmdbId(traktId)
    return null
  }
}
