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
    mediaId: Long,
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
    mediaId: Long,
    addToLimit: Int,
    tmdbId: Long? = null,
  ): List<Show>

  suspend fun fetchShowTranslations(
    mediaId: Long,
    code: String,
    tmdbId: Long? = null,
  ): List<Translation>

  suspend fun fetchNextEpisode(mediaId: Long): Episode?

  suspend fun fetchSeasons(
    mediaId: Long,
    tmdbId: Long? = null,
  ): List<Season>

  suspend fun fetchSeasonTranslations(
    showMediaId: Long,
    seasonNumber: Int,
    code: String,
  ): List<SeasonTranslation>

  // Movies

  suspend fun fetchMovie(
    mediaId: Long,
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
    mediaId: Long,
    addToLimit: Int,
    tmdbId: Long? = null,
  ): List<Movie>

  suspend fun fetchMovieTranslations(
    mediaId: Long,
    code: String,
    tmdbId: Long? = null,
  ): List<Translation>

  suspend fun fetchMovieCollections(mediaId: Long): List<MovieCollection>

  suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie>

  // People

  suspend fun fetchPersonShowsCredits(
    mediaId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long? = null,
  ): List<PersonCredit>

  suspend fun fetchPersonMoviesCredits(
    mediaId: Long,
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
