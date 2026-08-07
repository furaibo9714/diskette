package io.github.furaibo9714.diskette.ui_statistics.views.mostWatched

import io.github.furaibo9714.diskette.ui_base.common.ListItem
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation

data class StatisticsMostWatchedItem(
  override val show: Show,
  val episodes: List<Episode>,
  val seasonsCount: Long,
  override val image: Image,
  override val isLoading: Boolean = false,
  val translation: Translation? = null,
) : ListItem
