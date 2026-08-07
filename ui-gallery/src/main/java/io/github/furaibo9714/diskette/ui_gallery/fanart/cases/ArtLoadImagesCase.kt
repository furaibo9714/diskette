package io.github.furaibo9714.diskette.ui_gallery.fanart.cases

import io.github.furaibo9714.diskette.common.Config.FANART_GALLERY_IMAGES_LIMIT
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageFamily
import io.github.furaibo9714.diskette.ui_model.ImageFamily.MOVIE
import io.github.furaibo9714.diskette.ui_model.ImageFamily.SHOW
import io.github.furaibo9714.diskette.ui_model.ImageStatus.AVAILABLE
import io.github.furaibo9714.diskette.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArtLoadImagesCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  suspend fun loadImages(
    id: IdTrakt,
    family: ImageFamily,
    type: ImageType,
  ): List<Image> {
    val images = mutableListOf<Image>()
    val initialImage = loadInitialImage(id, family, type)
    if (initialImage.status == AVAILABLE) {
      images.add(initialImage)
    }

    var remoteImages: List<Image> = emptyList()
    if (family == SHOW) {
      val show = showsRepository.detailsShow.load(id)
      remoteImages = showImagesProvider.loadRemoteImages(show, type)
    } else if (family == MOVIE) {
      val movie = moviesRepository.movieDetails.load(id)
      remoteImages = movieImagesProvider.loadRemoteImages(movie, type)
    }
    images.addAll(remoteImages.filter { it.fullFileUrl != initialImage.fullFileUrl })
    return images.take(FANART_GALLERY_IMAGES_LIMIT)
  }

  private suspend fun loadInitialImage(
    id: IdTrakt,
    family: ImageFamily,
    type: ImageType,
  ) = when (family) {
    SHOW -> {
      val show = showsRepository.detailsShow.load(id)
      showImagesProvider.findCachedImage(show, type)
    }
    MOVIE -> {
      val movie = moviesRepository.movieDetails.load(id)
      movieImagesProvider.findCachedImage(movie, type)
    }
    else -> {
      throw IllegalStateException()
    }
  }
}
