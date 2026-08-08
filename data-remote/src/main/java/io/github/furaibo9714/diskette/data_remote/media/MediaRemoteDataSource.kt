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
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPerson

/**
 * Fetches show/movie metadata, search results and discovery feeds.
 */
interface MediaRemoteDataSource {

  // Shows

  suspend fun fetchShow(
    tmdbId: Long,
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
    tmdbId: Long,
    addToLimit: Int,
  ): List<Show>

  suspend fun fetchShowTranslations(
    tmdbId: Long,
    code: String,
  ): List<Translation>

  suspend fun fetchNextEpisode(tmdbId: Long): Episode?

  suspend fun fetchSeasons(
    tmdbId: Long,
  ): List<Season>

  suspend fun fetchSeasonTranslations(
    showTmdbId: Long,
    seasonNumber: Int,
    code: String,
  ): List<SeasonTranslation>

  // Movies

  suspend fun fetchMovie(
    tmdbId: Long,
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
    tmdbId: Long,
    addToLimit: Int,
  ): List<Movie>

  suspend fun fetchMovieTranslations(
    tmdbId: Long,
    code: String,
  ): List<Translation>

  suspend fun fetchMovieCollections(tmdbId: Long): List<MovieCollection>

  suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie>

  // People

  suspend fun fetchPersonShowsCredits(
    tmdbId: Long,
    type: TmdbPerson.Type,
  ): List<PersonCredit>

  suspend fun fetchPersonMoviesCredits(
    tmdbId: Long,
    type: TmdbPerson.Type,
  ): List<PersonCredit>

  // Search

  suspend fun fetchSearch(
    query: String,
    withMovies: Boolean,
  ): List<SearchResult>

  /** Resolves an external IMDb id to whichever show or movie it refers to, if any. */
  suspend fun findByImdbId(imdbId: String): SearchResult?
}
