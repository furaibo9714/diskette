package io.github.furaibo9714.diskette.ui_my_shows.myshows.recycler

import io.github.furaibo9714.diskette.ui_base.common.ListItem
import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_model.MyShowsSection
import io.github.furaibo9714.diskette.ui_model.Network
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.Translation

data class MyShowsItem(
  val type: Type,
  val header: Header?,
  val recentsSection: RecentsSection?,
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean,
  val spoilers: Spoilers,
  val translation: Translation? = null,
  val userRating: Int? = null,
  val sortOrder: SortOrder? = null,
) : ListItem {

  enum class Type {
    RECENT_SHOWS,
    ALL_SHOWS_HEADER,
    ALL_SHOWS_ITEM,
  }

  data class Header(
    val section: MyShowsSection,
    val itemCount: Int,
    val sortOrder: Pair<SortOrder, SortType>?,
    val networks: List<Network>?,
    val genres: List<Genre>?,
  )

  data class RecentsSection(
    val items: List<MyShowsItem>,
  )

  data class Spoilers(
    val isSpoilerHidden: Boolean,
    val isSpoilerRatingsHidden: Boolean,
    val isSpoilerTapToReveal: Boolean,
  )

  companion object {
    fun createHeader(
      section: MyShowsSection,
      itemCount: Int,
      sortOrder: Pair<SortOrder, SortType>?,
      networks: List<Network>?,
      genres: List<Genre>?,
    ) = MyShowsItem(
      type = Type.ALL_SHOWS_HEADER,
      header = Header(section, itemCount, sortOrder, networks, genres),
      recentsSection = null,
      show = Show.EMPTY,
      image = Image.createUnavailable(POSTER),
      isLoading = false,
      spoilers = Spoilers(
        isSpoilerHidden = false,
        isSpoilerRatingsHidden = false,
        isSpoilerTapToReveal = false,
      ),
    )

    fun createRecentsSection(shows: List<MyShowsItem>) =
      MyShowsItem(
        type = Type.RECENT_SHOWS,
        header = null,
        recentsSection = RecentsSection(shows),
        show = Show.EMPTY,
        image = Image.createUnavailable(POSTER),
        isLoading = false,
        spoilers = Spoilers(
          isSpoilerHidden = false,
          isSpoilerRatingsHidden = false,
          isSpoilerTapToReveal = false,
        ),
      )
  }
}
