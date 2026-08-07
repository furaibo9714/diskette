package io.github.furaibo9714.diskette.data_remote.trakt.api

import io.github.furaibo9714.diskette.data_remote.Config
import io.github.furaibo9714.diskette.data_remote.tmdb.model.TmdbPerson
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktMoviesService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktPeopleService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktSearchService
import io.github.furaibo9714.diskette.data_remote.trakt.api.service.TraktShowsService
import io.github.furaibo9714.diskette.data_remote.trakt.model.Episode
import io.github.furaibo9714.diskette.data_remote.trakt.model.Ids
import io.github.furaibo9714.diskette.data_remote.trakt.model.Movie
import io.github.furaibo9714.diskette.data_remote.trakt.model.MovieCollection
import io.github.furaibo9714.diskette.data_remote.trakt.model.PersonCredit
import io.github.furaibo9714.diskette.data_remote.trakt.model.Show

internal class TraktApi(
  private val showsService: TraktShowsService,
  private val moviesService: TraktMoviesService,
  private val searchService: TraktSearchService,
  private val peopleService: TraktPeopleService,
) : TraktRemoteDataSource {

  override suspend fun fetchShow(
    traktId: Long,
    tmdbId: Long?,
  ) = showsService.fetchShow(traktId)

  override suspend fun fetchShow(traktSlug: String) = showsService.fetchShow(traktSlug)

  override suspend fun fetchMovie(
    traktId: Long,
    tmdbId: Long?,
  ) = moviesService.fetchMovie(traktId)

  override suspend fun fetchMovie(traktSlug: String) = moviesService.fetchMovie(traktSlug)

  override suspend fun fetchPopularShows(
    genres: String,
    networks: String,
    limit: Int,
  ) = showsService.fetchPopularShows(genres, networks, limit)

  override suspend fun fetchPopularMovies(
    genres: String,
    limit: Int,
  ) = moviesService.fetchPopularMovies(genres, limit)

  override suspend fun fetchTrendingShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> = showsService.fetchTrendingShows(genres, networks, limit).map { it.show!! }

  override suspend fun fetchTrendingMovies(
    genres: String,
    limit: Int,
  ) = moviesService.fetchTrendingMovies(genres, limit).map { it.movie!! }

  override suspend fun fetchAnticipatedShows(
    genres: String,
    networks: String,
    limit: Int,
  ): List<Show> = showsService.fetchAnticipatedShows(genres, networks, limit).map { it.show!! }

  override suspend fun fetchAnticipatedMovies(
    genres: String,
    limit: Int,
  ): List<Movie> = moviesService.fetchAnticipatedMovies(genres, limit).map { it.movie!! }

  override suspend fun fetchRelatedShows(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ) = showsService.fetchRelatedShows(traktId, Config.TRAKT_RELATED_SHOWS_LIMIT + addToLimit)

  override suspend fun fetchRelatedMovies(
    traktId: Long,
    addToLimit: Int,
    tmdbId: Long?,
  ) = moviesService.fetchRelatedMovies(traktId, Config.TRAKT_RELATED_MOVIES_LIMIT + addToLimit)

  override suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = showsService.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  override suspend fun fetchSearch(
    query: String,
    withMovies: Boolean,
  ) = if (withMovies) {
    searchService.fetchSearchResultsMovies(query)
  } else {
    searchService.fetchSearchResults(query)
  }

  override suspend fun fetchPersonIds(
    idType: String,
    id: String,
  ): Ids? {
    val result = searchService.fetchPersonIds(idType, id)
    if (result.isNotEmpty()) {
      return result.first().person?.ids
    }
    return null
  }

  override suspend fun fetchPersonShowsCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "shows")
    val cast = result.cast ?: emptyList()
    val crew = result.crew
      ?.values
      ?.flatten()
      ?.distinctBy { it.show?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchPersonMoviesCredits(
    traktId: Long,
    type: TmdbPerson.Type,
    tmdbId: Long?,
  ): List<PersonCredit> {
    val result = peopleService.fetchPersonCredits(traktId = traktId, "movies")
    val cast = result.cast ?: emptyList()
    val crew = result.crew
      ?.values
      ?.flatten()
      ?.distinctBy { it.movie?.ids?.trakt } ?: emptyList()
    return if (type == TmdbPerson.Type.CAST) cast else crew
  }

  override suspend fun fetchSearchId(
    idType: String,
    id: String,
  ) = searchService.fetchSearchId(idType, id)

  override suspend fun fetchSeasons(
    traktId: Long,
    tmdbId: Long?,
  ) = showsService
    .fetchSeasons(traktId)
    .sortedByDescending { it.number }

  override suspend fun fetchShowTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long?,
  ) = showsService.fetchShowTranslations(traktId, code)

  override suspend fun fetchMovieTranslations(
    traktId: Long,
    code: String,
    tmdbId: Long?,
  ) = moviesService.fetchMovieTranslations(traktId, code)

  override suspend fun fetchSeasonTranslations(
    showTraktId: Long,
    seasonNumber: Int,
    code: String,
  ) = showsService.fetchSeasonTranslations(showTraktId, seasonNumber, code)

  override suspend fun fetchMovieCollections(traktId: Long): List<MovieCollection> {
    val lists = moviesService.fetchMovieCollections(traktId)
    return lists.filter { it.privacy == "public" }
  }

  override suspend fun fetchMovieCollectionItems(collectionId: Long): List<Movie> =
    moviesService
      .fetchMovieCollectionItems(collectionId)
      .sortedBy { it.rank }
      .map { it.movie }
}
