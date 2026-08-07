package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.movie.cases

import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuPinnedCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository,
) {

  fun addToTopPinned(traktId: IdTrakt) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.addPinnedItem(movie)
  }

  fun removeFromTopPinned(traktId: IdTrakt) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.removePinnedItem(movie)
  }
}
