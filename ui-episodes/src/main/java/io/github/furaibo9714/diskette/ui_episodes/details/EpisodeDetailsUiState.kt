package io.github.furaibo9714.diskette.ui_episodes.details

import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.RatingState
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_model.Translation
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class EpisodeDetailsUiState(
  val image: Image? = null,
  val isImageLoading: Boolean = false,
  val episodes: List<Episode>? = null,
  val lastWatchedAt: ZonedDateTime? = null,
  val rating: RatingState? = null,
  val translation: Translation? = null,
  val dateFormat: DateTimeFormatter? = null,
  val spoilers: SpoilersSettings? = null,
)
