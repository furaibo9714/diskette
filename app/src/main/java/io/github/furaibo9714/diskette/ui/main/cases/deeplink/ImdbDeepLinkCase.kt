package io.github.furaibo9714.diskette.ui.main.cases.deeplink

import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MovieDetailsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowDetailsRepository
import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkBundle
import io.github.furaibo9714.diskette.ui_model.IdImdb
import javax.inject.Inject

class ImdbDeepLinkCase @Inject constructor(
  private val traktRemoteSource: TraktRemoteDataSource,
  private val showsLocalSource: ShowsLocalDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val showDetailsRepository: ShowDetailsRepository,
  private val movieDetailsRepository: MovieDetailsRepository,
  private val mappers: Mappers,
) {

  companion object {
    private const val SEARCH_ID_TYPE = "imdb"
  }

  suspend fun findById(imdbId: IdImdb): DeepLinkBundle {
    val show = showDetailsRepository.find(imdbId)
    if (show != null) {
      return DeepLinkBundle(show = show)
    }

    val movie = movieDetailsRepository.find(imdbId)
    if (movie != null) {
      return DeepLinkBundle(movie = movie)
    }

    val searchResult = traktRemoteSource.fetchSearchId(SEARCH_ID_TYPE, imdbId.id)
    if (searchResult.size == 1) {
      val showSearch = searchResult[0].show
      val movieSearch = searchResult[0].movie
      when {
        showSearch != null -> {
          val uiShow = mappers.show.fromNetwork(showSearch)
          showsLocalSource.upsert(listOf(mappers.show.toDatabase(uiShow)))
          return DeepLinkBundle(show = uiShow)
        }
        movieSearch != null -> {
          val uiMovie = mappers.movie.fromNetwork(movieSearch)
          moviesLocalSource.upsert(listOf(mappers.movie.toDatabase(uiMovie)))
          return DeepLinkBundle(movie = uiMovie)
        }
      }
    }

    return DeepLinkBundle.EMPTY
  }
}
