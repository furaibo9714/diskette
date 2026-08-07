package io.github.furaibo9714.diskette.ui_my_movies.mymovies.helpers

import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortOrder.DATE_ADDED
import io.github.furaibo9714.diskette.ui_model.SortOrder.NAME
import io.github.furaibo9714.diskette.ui_model.SortOrder.NEWEST
import io.github.furaibo9714.diskette.ui_model.SortOrder.RANDOM
import io.github.furaibo9714.diskette.ui_model.SortOrder.RATING
import io.github.furaibo9714.diskette.ui_model.SortOrder.RUNTIME
import io.github.furaibo9714.diskette.ui_model.SortOrder.USER_RATING
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.SortType.ASCENDING
import io.github.furaibo9714.diskette.ui_model.SortType.DESCENDING
import io.github.furaibo9714.diskette.ui_my_movies.mymovies.recycler.MyMoviesItem
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMoviesSorter @Inject constructor() {

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
      RATING -> {
        compareBy { it.movie.rating }
      }
      USER_RATING -> {
        compareByDescending<MyMoviesItem> { it.userRating != null }
          .thenBy { it.userRating }
          .thenBy { getTitle(it) }
      }
      DATE_ADDED -> {
        compareBy { it.movie.updatedAt }
      }
      RUNTIME -> {
        compareBy { it.movie.runtime }
      }
      NEWEST -> {
        compareBy<MyMoviesItem> { it.movie.year }.thenBy { it.movie.released }
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
      RATING -> {
        compareByDescending { it.movie.rating }
      }
      USER_RATING -> {
        compareByDescending<MyMoviesItem> { it.userRating != null }
          .thenByDescending { it.userRating }
          .thenBy { getTitle(it) }
      }
      DATE_ADDED -> {
        compareByDescending { it.movie.updatedAt }
      }
      RUNTIME -> {
        compareByDescending { it.movie.runtime }
      }
      NEWEST -> {
        compareByDescending<MyMoviesItem> { it.movie.year }.thenByDescending { it.movie.released }
      }
      RANDOM -> {
        compareBy { UUID.randomUUID() }
      }
      else -> {
        throw IllegalStateException("Invalid sort order")
      }
    }

  private fun getTitle(item: MyMoviesItem): String {
    val translatedTitle =
      if (item.translation?.hasTitle == true) {
        item.translation.title
      } else {
        item.movie.titleNoThe
      }
    return translatedTitle.uppercase()
  }
}
