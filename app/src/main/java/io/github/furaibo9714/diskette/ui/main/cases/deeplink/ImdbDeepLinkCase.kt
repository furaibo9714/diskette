package io.github.furaibo9714.diskette.ui.main.cases.deeplink

import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.repository.movies.MovieDetailsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowDetailsRepository
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkBundle
import javax.inject.Inject

class ImdbDeepLinkCase @Inject constructor(
  private val mediaRemoteSource: MediaRemoteDataSource,
  private val showsLocalSource: ShowsLocalDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val showDetailsRepository: ShowDetailsRepository,
  private val movieDetailsRepository: MovieDetailsRepository,
  private val mappers: Mappers,
) {

  suspend fun findById(imdbId: IdImdb): DeepLinkBundle {
    val show = showDetailsRepository.find(imdbId)
    if (show != null) {
      return DeepLinkBundle(show = show)
    }

    val movie = movieDetailsRepository.find(imdbId)
    if (movie != null) {
      return DeepLinkBundle(movie = movie)
    }

    val searchResult = mediaRemoteSource.findByImdbId(imdbId.id) ?: return DeepLinkBundle.EMPTY
    val showSearch = searchResult.show
    val movieSearch = searchResult.movie
    return when {
      showSearch != null -> {
        val uiShow = mappers.show.fromNetwork(showSearch)
        showsLocalSource.upsert(listOf(mappers.show.toDatabase(uiShow)))
        DeepLinkBundle(show = uiShow)
      }
      movieSearch != null -> {
        val uiMovie = mappers.movie.fromNetwork(movieSearch)
        moviesLocalSource.upsert(listOf(mappers.movie.toDatabase(uiMovie)))
        DeepLinkBundle(movie = uiMovie)
      }
      else -> DeepLinkBundle.EMPTY
    }
  }
}
