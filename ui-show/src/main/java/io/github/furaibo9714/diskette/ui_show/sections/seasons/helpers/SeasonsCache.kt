package io.github.furaibo9714.diskette.ui_show.sections.seasons.helpers

import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper memory cache of seasons for a current show details.
 */
@Singleton
class SeasonsCache @Inject constructor() {

  private val seasonsCache = Collections.synchronizedMap(mutableMapOf<MediaId, SeasonsBundle?>())

  fun setSeasons(
    showId: MediaId,
    seasons: List<SeasonListItem>,
    areSeasonsLocal: Boolean,
  ) {
    seasonsCache[showId] = SeasonsBundle(seasons.toList(), areSeasonsLocal)
  }

  fun loadSeasons(showId: MediaId): List<SeasonListItem>? = seasonsCache[showId]?.seasons

  fun hasSeasons(showId: MediaId): Boolean = seasonsCache[showId]?.seasons != null

  fun areSeasonsLocal(showId: MediaId): Boolean = seasonsCache[showId]?.isLocal ?: false

  fun clear(showId: MediaId) {
    seasonsCache.remove(showId)
  }
}
