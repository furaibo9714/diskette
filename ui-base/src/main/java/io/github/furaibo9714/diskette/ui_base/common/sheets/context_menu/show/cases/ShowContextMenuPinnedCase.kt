package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases

import io.github.furaibo9714.diskette.repository.OnHoldItemsRepository
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuPinnedCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
) {

  fun addToTopPinned(traktId: IdTrakt) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    onHoldItemsRepository.removeItem(show)
    pinnedItemsRepository.addPinnedItem(show)
  }

  fun removeFromTopPinned(traktId: IdTrakt) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.removePinnedItem(show)
  }
}
