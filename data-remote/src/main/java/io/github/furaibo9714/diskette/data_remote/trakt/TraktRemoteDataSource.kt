package io.github.furaibo9714.diskette.data_remote.trakt

import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPerson
import io.github.furaibo9714.diskette.data_remote.trakt.model.Episode
import io.github.furaibo9714.diskette.data_remote.trakt.model.Movie
import io.github.furaibo9714.diskette.data_remote.trakt.model.MovieCollection
import io.github.furaibo9714.diskette.data_remote.trakt.model.PersonCredit
import io.github.furaibo9714.diskette.data_remote.trakt.model.SearchResult
import io.github.furaibo9714.diskette.data_remote.trakt.model.Season
import io.github.furaibo9714.diskette.data_remote.trakt.model.SeasonTranslation
import io.github.furaibo9714.diskette.data_remote.trakt.model.Show
import io.github.furaibo9714.diskette.data_remote.trakt.model.Translation

/**
 * Fetch remote show/movie metadata, search and discovery. Named for the Trakt API it was
 * originally shaped around; it is served entirely from TMDB now.
 */
interface TraktRemoteDataSource {

  // Shows

  suspend fun fetchShow(
    traktId: Long,
    tmdbId: Long? = null,
  ): Show

  suspend fun fetchPopularShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show>

  suspend fun fetchTrendingShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show>

  suspend fun fetchAnticipatedShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show>

  suspend fun fetchRelatedShows(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long? = null,
  ): List<Show>

  suspend fun fetchShowTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long? = null,
  ): List<Translation>

  suspend fun fetchNextEpisode(traktId: Long): Episode?

  suspend fun fetchSeasons(
    traktId: Long,
    tmdbId: Long? = null,
  ): List<Season>

  suspend fun fetchSeasonTranslations(
    showTraktId: Long,
    seasonNumber: Int,
    code: String,
  ): List<SeasonTranslation>

  // Movies

  suspend fun fetchMovie(
    traktId: Long,
    tmdbId: Long? = null,
  ): Movie

  suspend fun fetchPopularMovies(
    genres: String,
    limit: Int,
  ): List<Movie>

  suspend fun fetchTrendingMovies(
    genres: String,
    limit: Int,
  ): List<Movie>

  suspend fun fetchAnticipatedMovies(
    genres: String,
    limit: Int,
  ): List<Movie>

  suspend fun fetchRelatedMovies(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long? = null,
  ): List<Movie>

  suspend fun fetchMovieTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long? = null,
  ): List<Translation>

  suspend fun fetchMovieCollections(traktId: Long): List<MovieCollection>

  suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie>

  // People

  suspend fun fetchPersonShowsCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long? = null,
  ): List<PersonCredit>

  suspend fun fetchPersonMoviesCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long? = null,
  ): List<PersonCredit>

  // Search

  suspend fun fetchSearch(
    query: String,
    withMovies: Boolean,
  ): List<SearchResult>

  /** Resolves an external IMDb id to whichever show or movie it refers to, if any. */
  suspend fun findByImdbId(imdbId: String): SearchResult?
}
