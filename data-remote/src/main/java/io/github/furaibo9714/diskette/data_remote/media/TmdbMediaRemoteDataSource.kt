package io.github.furaibo9714.diskette.data_remote.media

import io.github.furaibo9714.diskette.data_remote.media.model.Episode
import io.github.furaibo9714.diskette.data_remote.media.model.Movie
import io.github.furaibo9714.diskette.data_remote.media.model.MovieCollection
import io.github.furaibo9714.diskette.data_remote.media.model.PersonCredit
import io.github.furaibo9714.diskette.data_remote.media.model.SearchResult
import io.github.furaibo9714.diskette.data_remote.media.model.Season
import io.github.furaibo9714.diskette.data_remote.media.model.SeasonTranslation
import io.github.furaibo9714.diskette.data_remote.media.model.Show
import io.github.furaibo9714.diskette.data_remote.media.model.Translation
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbGenreSlugs
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbModelConverter
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbSyntheticIds
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbMoviesService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbSearchService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbService
import io.github.furaibo9714.diskette.data_remote.tmdb.api.TmdbShowsService
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPerson
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbSearchMultiResponse
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Serves the whole [MediaRemoteDataSource] surface from TMDB. */
internal class TmdbMediaRemoteDataSource(
  private val tmdbSearch: TmdbSearchService,
  private val tmdbShows: TmdbShowsService,
  private val tmdbMovies: TmdbMoviesService,
  private val tmdbPeople: TmdbService,
) : MediaRemoteDataSource {

  companion object {
    private const val RELATED_PAGES = 2
    private const val PAGE_SIZE = 20
    private const val MAX_PAGES = 5
    private const val SPECIALS_SEASON = 0
    private const val IMDB_EXTERNAL_SOURCE = "imdb_id"
    private const val MEDIA_TYPE_MOVIE = "movie"
    private const val MEDIA_TYPE_TV = "tv"
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
      TmdbModelConverter.toSearchResult(item, order = index + 1)
    }
  }

  override suspend fun fetchShow(
    mediaId: Long,
    tmdbId: Long?,
  ): Show {
    val resolvedTmdbId = requireTmdbId(mediaId, tmdbId)
    val details = tmdbShows.fetchShowDetails(resolvedTmdbId)
    val show = TmdbModelConverter.toShow(resolvedTmdbId, details)
    if (!details.episode_run_time.isNullOrEmpty()) return show

    /**
     * Season 0 holds specials - recaps, minisodes, behind-the-scenes - whose lengths say nothing
     * about how long a normal episode runs, and which would otherwise drag the low end of the
     * range down to a couple of minutes.
     */
    val seasonNumbers = details.seasons
      .orEmpty()
      .mapNotNull { it.season_number }
      .filter { it != SPECIALS_SEASON }
    val episodeRuntimes = coroutineScope {
      seasonNumbers
        .map { seasonNumber -> async { tmdbShows.fetchSeasonDetails(resolvedTmdbId, seasonNumber) } }
        .awaitAll()
        .flatMap { it.episodes.orEmpty() }
        .mapNotNull { it.runtime }
        .filter { it > 0 }
    }
    val runtime = episodeRuntimes.minOrNull() ?: return show
    val runtimeMax = episodeRuntimes.maxOrNull()?.takeIf { it != runtime }
    return show.copy(runtime = runtime, runtime_max = runtimeMax)
  }

  override suspend fun fetchMovie(
    mediaId: Long,
    tmdbId: Long?,
  ): Movie {
    val resolvedTmdbId = requireTmdbId(mediaId, tmdbId)
    val details = tmdbMovies.fetchMovieDetails(resolvedTmdbId)
    return TmdbModelConverter.toMovie(resolvedTmdbId, details)
  }

  override suspend fun fetchShowTranslations(
    mediaId: Long,
    code: String,
    tmdbId: Long?,
  ): List<Translation> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    val response = tmdbShows.fetchShowTranslations(resolvedTmdbId)
    return TmdbModelConverter.toShowTranslations(response).filter { it.language == code }
  }

  override suspend fun fetchMovieTranslations(
    mediaId: Long,
    code: String,
    tmdbId: Long?,
  ): List<Translation> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    val response = tmdbMovies.fetchMovieTranslations(resolvedTmdbId)
    return TmdbModelConverter.toMovieTranslations(response).filter { it.language == code }
  }

  override suspend fun fetchSeasons(
    mediaId: Long,
    tmdbId: Long?,
  ): List<Season> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    val showDetails = tmdbShows.fetchShowDetails(resolvedTmdbId)
    val seasonNumbers = showDetails.seasons.orEmpty().mapNotNull { it.season_number }
    return coroutineScope {
      seasonNumbers
        .map { seasonNumber -> async { tmdbShows.fetchSeasonDetails(resolvedTmdbId, seasonNumber) } }
        .awaitAll()
        .map { TmdbModelConverter.toSeason(it) }
        .sortedByDescending { it.number }
    }
  }

  override suspend fun findByImdbId(imdbId: String): SearchResult? {
    val response = tmdbSearch.findByExternalId(imdbId, externalSource = IMDB_EXTERNAL_SOURCE)
    response.tv_results.orEmpty().firstOrNull()?.let { item ->
      return TmdbModelConverter.toSearchResult(item.copy(media_type = MEDIA_TYPE_TV), order = 1)
    }
    response.movie_results.orEmpty().firstOrNull()?.let { item ->
      return TmdbModelConverter.toSearchResult(item.copy(media_type = MEDIA_TYPE_MOVIE), order = 1)
    }
    return null
  }

  override suspend fun fetchNextEpisode(mediaId: Long): Episode? {
    val resolvedTmdbId = resolveTmdbId(mediaId, null) ?: return null
    val details = tmdbShows.fetchShowDetails(resolvedTmdbId)
    return details.next_episode_to_air?.let { TmdbModelConverter.toEpisode(it) }
  }

  /** TMDB returns a season's episodes already localised when the request carries a `language`. */
  override suspend fun fetchSeasonTranslations(
    showMediaId: Long,
    seasonNumber: Int,
    code: String,
  ): List<SeasonTranslation> {
    val resolvedTmdbId = resolveTmdbId(showMediaId, null) ?: return emptyList()
    val details = tmdbShows.fetchSeasonDetails(resolvedTmdbId, seasonNumber, language = code)
    return TmdbModelConverter.toSeasonTranslations(details, code)
  }

  /**
   * A TMDB movie belongs to at most one collection, so this returns either zero or one entry.
   */
  override suspend fun fetchMovieCollections(mediaId: Long): List<MovieCollection> {
    val resolvedTmdbId = resolveTmdbId(mediaId, null) ?: return emptyList()
    val details = tmdbMovies.fetchMovieDetails(resolvedTmdbId)
    val collection = details.belongs_to_collection ?: return emptyList()
    return listOfNotNull(TmdbModelConverter.toMovieCollection(collection))
  }

  override suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie> {
    val resolvedTmdbId = resolveTmdbId(collectionId, null) ?: return emptyList()
    val details = tmdbMovies.fetchCollection(resolvedTmdbId)
    return details.parts.orEmpty().mapNotNull { part ->
      part.id?.let { TmdbModelConverter.toMovie(it, part) }
    }
  }

  override suspend fun fetchRelatedShows(
    mediaId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ): List<Show> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    return fetchPages(RELATED_PAGES) { page -> tmdbShows.fetchSimilarShows(resolvedTmdbId, page) }
      .mapNotNull { TmdbModelConverter.toShowSummary(it) }
  }

  override suspend fun fetchRelatedMovies(
    mediaId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ): List<Movie> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    return fetchPages(RELATED_PAGES) { page -> tmdbMovies.fetchSimilarMovies(resolvedTmdbId, page) }
      .mapNotNull { TmdbModelConverter.toMovieSummary(it) }
  }

  override suspend fun fetchPersonShowsCredits(
    mediaId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    val response = tmdbPeople.fetchPersonTvCredits(resolvedTmdbId)
    return TmdbModelConverter.toPersonShowCredits(response, isCast = type == TmdbPerson.Type.CAST)
  }

  override suspend fun fetchPersonMoviesCredits(
    mediaId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val resolvedTmdbId = resolveTmdbId(mediaId, tmdbId) ?: return emptyList()
    val response = tmdbPeople.fetchPersonMovieCredits(resolvedTmdbId)
    return TmdbModelConverter.toPersonMovieCredits(response, isCast = type == TmdbPerson.Type.CAST)
  }

  /**
   * TMDB's `trending`/`popular` endpoints don't support server-side genre filtering, so genres are
   * filtered client-side after conversion. `discover` supports `with_genres` server-side.
   * The `networks` filter is dropped: Diskette's `Network` enum encodes broadcaster names as
   * free-text aliases, which have no translation to TMDB's numeric `with_networks` ids.
   */
  override suspend fun fetchTrendingShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> =
    fetchPages(pagesFor(limit)) { page -> tmdbShows.fetchTrendingShows(page = page) }
      .mapNotNull { TmdbModelConverter.toShowSummary(it) }
      .filterShowsByGenres(genres)

  override suspend fun fetchPopularShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> =
    fetchPages(pagesFor(limit)) { page -> tmdbShows.fetchPopularShows(page = page) }
      .mapNotNull { TmdbModelConverter.toShowSummary(it) }
      .filterShowsByGenres(genres)

  override suspend fun fetchAnticipatedShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> {
    val genreIds = TmdbGenreSlugs.tvGenreIds(genres.toSlugList()).joinToString(",").ifBlank { null }
    return fetchPages(pagesFor(limit)) { page ->
      tmdbShows.discoverShows(page = page, withGenres = genreIds, firstAirDateFrom = today())
    }.mapNotNull { TmdbModelConverter.toShowSummary(it) }
  }

  override suspend fun fetchTrendingMovies(
    genres: String,
    limit: Int,
  ): List<Movie> =
    fetchPages(pagesFor(limit)) { page -> tmdbMovies.fetchTrendingMovies(page = page) }
      .mapNotNull { TmdbModelConverter.toMovieSummary(it) }
      .filterMoviesByGenres(genres)

  override suspend fun fetchPopularMovies(
    genres: String,
    limit: Int,
  ): List<Movie> =
    fetchPages(pagesFor(limit)) { page -> tmdbMovies.fetchPopularMovies(page = page) }
      .mapNotNull { TmdbModelConverter.toMovieSummary(it) }
      .filterMoviesByGenres(genres)

  override suspend fun fetchAnticipatedMovies(
    genres: String,
    limit: Int,
  ): List<Movie> {
    val genreIds = TmdbGenreSlugs.movieGenreIds(genres.toSlugList()).joinToString(",").ifBlank { null }
    return fetchPages(pagesFor(limit)) { page ->
      tmdbMovies.discoverMovies(page = page, withGenres = genreIds, releaseDateFrom = today())
    }.mapNotNull { TmdbModelConverter.toMovieSummary(it) }
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
   * Media ids are synthetic (see [TmdbSyntheticIds]) and reverse trivially to the real TMDB id.
   * Callers that already hold a TMDB id from a local row can pass it directly instead. A null
   * result means the caller supplied neither, which leaves nothing to fetch.
   */
  private fun resolveTmdbId(
    mediaId: Long,
    tmdbId: Long?,
  ): Long? {
    if (tmdbId != null && tmdbId > 0) return tmdbId
    if (TmdbSyntheticIds.isSynthetic(mediaId)) return TmdbSyntheticIds.toTmdbId(mediaId)
    return null
  }

  private fun requireTmdbId(
    mediaId: Long,
    tmdbId: Long?,
  ): Long =
    resolveTmdbId(mediaId, tmdbId)
      ?: error("No TMDB id available for id=$mediaId. Cannot fetch details.")
}
