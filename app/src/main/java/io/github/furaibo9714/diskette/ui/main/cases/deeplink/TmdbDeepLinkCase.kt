package io.github.furaibo9714.diskette.ui.main.cases.deeplink

import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbSyntheticIds
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

  /**
   * The TMDB id is already the provider's own id, so the remote lookup fetches the title directly
   * rather than searching for it.
   */
  suspend fun findById(
    tmdbId: IdTmdb,
    type: String,
  ): DeepLinkBundle {
    if (type == TMDB_TYPE_TV) {
      showDetailsRepository.find(tmdbId)?.let { return DeepLinkBundle(show = it) }

      val show = traktRemoteSource.fetchShow(TmdbSyntheticIds.toSyntheticTraktId(tmdbId.id), tmdbId.id)
      val uiShow = mappers.show.fromNetwork(show)
      showsLocalSource.upsert(listOf(mappers.show.toDatabase(uiShow)))
      return DeepLinkBundle(show = uiShow)
    }

    if (type == TMDB_TYPE_MOVIE) {
      movieDetailsRepository.find(tmdbId)?.let { return DeepLinkBundle(movie = it) }

      val movie = traktRemoteSource.fetchMovie(TmdbSyntheticIds.toSyntheticTraktId(tmdbId.id), tmdbId.id)
      val uiMovie = mappers.movie.fromNetwork(movie)
      moviesLocalSource.upsert(listOf(mappers.movie.toDatabase(uiMovie)))
      return DeepLinkBundle(movie = uiMovie)
    }

    return DeepLinkBundle.EMPTY
  }
}
