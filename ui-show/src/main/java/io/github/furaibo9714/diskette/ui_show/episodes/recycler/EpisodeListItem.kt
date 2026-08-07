package io.github.furaibo9714.diskette.ui_show.episodes.recycler

import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.TraktRating
import io.github.furaibo9714.diskette.ui_model.Translation
import java.time.format.DateTimeFormatter

data class EpisodeListItem(
  val episode: Episode,
  val season: Season,
  val isWatched: Boolean,
  val translation: Translation? = null,
  val myRating: TraktRating? = null,
  val dateFormat: DateTimeFormatter? = null,
  val isLocked: Boolean = true,
  val isAnime: Boolean = false,
  val spoilers: SpoilersSettings,
) {

  val id = episode.ids.trakt.id
}
