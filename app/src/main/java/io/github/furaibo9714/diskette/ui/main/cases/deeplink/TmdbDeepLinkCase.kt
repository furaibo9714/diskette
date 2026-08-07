package io.github.furaibo9714.diskette.ui.main.cases.deeplink

import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MovieDetailsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowDetailsRepository
import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkBundle
import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkResolver.Companion.TMDB_TYPE_MOVIE
import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkResolver.Companion.TMDB_TYPE_TV
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import javax.inject.Inject

class TmdbDeepLinkCase @Inject constructor(
  private val traktRemoteSource: TraktRemoteDataSource,
  private val showsLocalSource: ShowsLocalDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val showDetailsRepository: ShowDetailsRepository,
  private val movieDetailsRepository: MovieDetailsRepository,
  private val mappers: Mappers,
) {

  companion object {
    private const val SEARCH_ID_TYPE = "tmdb"
  }

  suspend fun findById(
    tmdbId: IdTmdb,
    type: String,
  ): DeepLinkBundle {
    val localShow = showDetailsRepository.find(tmdbId)
    if (localShow != null && type == TMDB_TYPE_TV) {
      return DeepLinkBundle(show = localShow)
    }

    val localMovie = movieDetailsRepository.find(tmdbId)
    if (localMovie != null && type == TMDB_TYPE_MOVIE) {
      return DeepLinkBundle(movie = localMovie)
    }

    val searchResult = traktRemoteSource.fetchSearchId(SEARCH_ID_TYPE, tmdbId.id.toString())
    if (searchResult.isNotEmpty()) {
      searchResult
        .filter { it.show != null || it.movie != null }
        .forEach { result ->
          val show = result.show
          val movie = result.movie
          if (show != null && type == TMDB_TYPE_TV) {
            val uiShow = mappers.show.fromNetwork(show)
            showsLocalSource.upsert(listOf(mappers.show.toDatabase(uiShow)))
            return DeepLinkBundle(show = uiShow)
          }
          if (movie != null && type == TMDB_TYPE_MOVIE) {
            val uiMovie = mappers.movie.fromNetwork(movie)
            moviesLocalSource.upsert(listOf(mappers.movie.toDatabase(uiMovie)))
            return DeepLinkBundle(movie = uiMovie)
          }
        }
    }

    return DeepLinkBundle.EMPTY
  }
}
