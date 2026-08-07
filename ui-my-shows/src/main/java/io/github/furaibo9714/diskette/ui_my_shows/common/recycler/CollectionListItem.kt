package io.github.furaibo9714.diskette.ui_my_shows.common.recycler

import io.github.furaibo9714.diskette.common.extensions.toZonedDateTime
import io.github.furaibo9714.diskette.ui_base.common.ListItem
import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Network
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SortOrder
import io.github.furaibo9714.diskette.ui_model.SortType
import io.github.furaibo9714.diskette.ui_model.Translation
import io.github.furaibo9714.diskette.ui_model.UpcomingFilter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

sealed class CollectionListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  fun getReleaseDate(): ZonedDateTime? = show.firstAired.toZonedDateTime()

  data class ShowItem(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val dateFormat: DateTimeFormatter,
    val translation: Translation? = null,
    val userRating: Int? = null,
    val sortOrder: SortOrder? = null,
    val spoilers: Spoilers,
  ) : CollectionListItem(
      show = show,
      image = image,
      isLoading = isLoading,
    ) {

    data class Spoilers(
      val isSpoilerHidden: Boolean,
      val isSpoilerRatingsHidden: Boolean,
      val isSpoilerTapToReveal: Boolean,
    )
  }

  data class FiltersItem(
    val sortOrder: SortOrder,
    val sortType: SortType,
    val networks: List<Network>,
    val genres: List<Genre>,
    val upcoming: UpcomingFilter,
    val count: Int,
  ) : CollectionListItem(
      show = Show.EMPTY,
      image = Image.createUnknown(ImageType.POSTER),
      isLoading = false,
    ) {

    fun hasActiveFilters() = upcoming.isActive() || networks.isNotEmpty() || genres.isNotEmpty()
  }
}
