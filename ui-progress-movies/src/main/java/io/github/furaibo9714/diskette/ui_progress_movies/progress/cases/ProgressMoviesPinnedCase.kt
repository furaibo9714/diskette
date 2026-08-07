package io.github.furaibo9714.diskette.ui_progress_movies.progress.cases

import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressMoviesPinnedCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository,
) {

  fun addPinnedItem(item: Movie) = pinnedItemsRepository.addPinnedItem(item)

  fun removePinnedItem(item: Movie) = pinnedItemsRepository.removePinnedItem(item)
}
