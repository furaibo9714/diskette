package io.github.furaibo9714.diskette.ui_progress.helpers

import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortOrder.EPISODES_LEFT
import io.github.furaibo9714.diskette.ui_model.SortOrder.NAME
import io.github.furaibo9714.diskette.ui_model.SortOrder.NEWEST
import io.github.furaibo9714.diskette.ui_model.SortOrder.RANDOM
import io.github.furaibo9714.diskette.ui_model.SortOrder.RATING
import io.github.furaibo9714.diskette.ui_model.SortOrder.RECENTLY_WATCHED
import io.github.furaibo9714.diskette.ui_model.SortOrder.USER_RATING
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SortType.ASCENDING
import io.github.furaibo9714.diskette.ui_model.SortType.DESCENDING
import io.github.furaibo9714.diskette.ui_progress.progress.recycler.ProgressListItem
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressItemsSorter @Inject constructor() {
  fun sort(
    sortOrder: SortOrder,
    sortType: SortType,
  ) = when (sortType) {
    ASCENDING -> sortAscending(sortOrder)
    DESCENDING -> sortDescending(sortOrder)
  }

  private fun sortAscending(sortOrder: SortOrder) =
    when (sortOrder) {
      NAME -> {
        compareBy { getTitle(it) }
      }
      RECENTLY_WATCHED -> {
        compareBy { it.show.updatedAt }
      }
      NEWEST -> {
        compareBy { it.episode?.firstAired?.toMillis() }
      }
      RATING -> {
        compareBy { it.show.rating }
      }
      USER_RATING -> {
        compareByDescending<ProgressListItem.Episode> { it.userRating != null }
          .thenBy { it.userRating }
          .thenBy { getTitle(it) }
      }
      EPISODES_LEFT -> {
        compareBy<ProgressListItem.Episode> { it.totalCount - it.watchedCount }.thenBy { getTitle(it) }
      }
      RANDOM -> {
        compareBy { UUID.randomUUID() }
      }
      else -> {
        throw IllegalStateException("Invalid sort order")
      }
    }

  private fun sortDescending(sortOrder: SortOrder) =
    when (sortOrder) {
      NAME -> {
        compareByDescending { getTitle(it) }
      }
      RECENTLY_WATCHED -> {
        compareByDescending { it.show.updatedAt }
      }
      NEWEST -> {
        compareByDescending { it.episode?.firstAired?.toMillis() }
      }
      RATING -> {
        compareByDescending { it.show.rating }
      }
      USER_RATING -> {
        compareByDescending<ProgressListItem.Episode> { it.userRating != null }
          .thenByDescending { it.userRating }
          .thenBy { getTitle(it) }
      }
      EPISODES_LEFT -> {
        compareByDescending<ProgressListItem.Episode> {
          it.totalCount - it.watchedCount
        }.thenBy { getTitle(it) }
      }
      RANDOM -> {
        compareBy { UUID.randomUUID() }
      }
      else -> {
        throw IllegalStateException("Invalid sort order")
      }
    }

  private fun getTitle(item: ProgressListItem.Episode): String {
    val translatedTitle =
      if (item.translations?.show?.hasTitle == false) {
        null
      } else {
        item.translations?.show?.title
      }
    return (translatedTitle ?: item.show.titleNoThe).uppercase(Locale.ROOT)
  }
}
