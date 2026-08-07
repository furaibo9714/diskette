package io.github.furaibo9714.diskette.ui_show.sections.seasons.helpers

import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_show.sections.seasons.recycler.SeasonListItem
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper memory cache of seasons for a current show details.
 */
@Singleton
class SeasonsCache @Inject constructor() {

  private val seasonsCache = Collections.synchronizedMap(mutableMapOf<IdTrakt, SeasonsBundle?>())

  fun setSeasons(
    showId: IdTrakt,
    seasons: List<SeasonListItem>,
    areSeasonsLocal: Boolean,
  ) {
    seasonsCache[showId] = SeasonsBundle(seasons.toList(), areSeasonsLocal)
  }

  fun loadSeasons(showId: IdTrakt): List<SeasonListItem>? = seasonsCache[showId]?.seasons

  fun hasSeasons(showId: IdTrakt): Boolean = seasonsCache[showId]?.seasons != null

  fun areSeasonsLocal(showId: IdTrakt): Boolean = seasonsCache[showId]?.isLocal ?: false

  fun clear(showId: IdTrakt) {
    seasonsCache.remove(showId)
  }
}
