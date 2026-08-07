package io.github.furaibo9714.diskette.ui_search.helpers

import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_search.recycler.SearchListItem
import java.util.UUID

object TestData {

  val SEARCH_LIST_ITEM = SearchListItem(
    id = UUID.randomUUID(),
    show = Show.EMPTY,
    movie = Movie.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    translation = null,
    order = 0,
    isFollowed = false,
    isLoading = false,
    isWatchlist = false,
    spoilers = SpoilersSettings.INITIAL,
  )
}
