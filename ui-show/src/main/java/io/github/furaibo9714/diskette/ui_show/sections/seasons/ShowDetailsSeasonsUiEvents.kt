@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_show.sections.seasons

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_show.quicksetup.QuickSetupListItem

sealed class ShowDetailsSeasonsEvent<T>(
  action: T,
) : Event<T>(action) {

  data class OpenSeasonEpisodes(
    val showId: MediaId,
    val seasonId: MediaId,
  ) : ShowDetailsSeasonsEvent<MediaId>(showId)

  data class OpenSeasonDateSelection(
    val season: Season,
  ) : ShowDetailsSeasonsEvent<Season>(season)

  data class OpenQuickProgressDateSelection(
    val item: QuickSetupListItem,
  ) : ShowDetailsSeasonsEvent<QuickSetupListItem>(item)

  object RequestWidgetsUpdate : ShowDetailsSeasonsEvent<Unit>(Unit)
}
