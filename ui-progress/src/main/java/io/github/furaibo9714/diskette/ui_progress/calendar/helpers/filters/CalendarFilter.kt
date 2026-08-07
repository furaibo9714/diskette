package io.github.furaibo9714.diskette.ui_progress.calendar.helpers.filters

import io.github.furaibo9714.diskette.data_local.database.model.Episode
import java.time.ZonedDateTime

interface CalendarFilter {
  fun filter(
    now: ZonedDateTime,
    episode: Episode,
    onlyPremieres: Boolean,
  ): Boolean
}
