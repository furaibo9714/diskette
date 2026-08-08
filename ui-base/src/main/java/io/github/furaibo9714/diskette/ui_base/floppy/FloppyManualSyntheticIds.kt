package io.github.furaibo9714.diskette.ui_base.floppy

import java.util.UUID

/**
 * Floppy "manual" entries (tracked media with no external provider id, just a title) are
 * identified by a Floppy-generated UUID, not a linear numeric id like TMDB's - so unlike
 * `TmdbSyntheticIds` there's no simple offset-and-subtract reversal. Instead: derive a synthetic
 * id_trakt from the UUID's bits (offset kept safely clear of TmdbSyntheticIds' range, which tops
 * out around 1,001,500,000 given TMDB's current id space), and store the real UUID in
 * Show/Movie's otherwise-unused id_slug column so it's recoverable when addressing the item back
 * to Floppy.
 */
object FloppyManualSyntheticIds {

  private const val OFFSET = 3_000_000_000L
  private const val SLUG_PREFIX = "floppy-manual:"

  fun toSyntheticTraktId(floppyMediaId: String): Long {
    val bits = UUID.fromString(floppyMediaId).leastSignificantBits and 0x0FFFFFFFFFFFFFFFL
    return OFFSET + bits
  }

  fun isSynthetic(mediaId: Long): Boolean = mediaId >= OFFSET

  fun toSlugValue(floppyMediaId: String): String = "$SLUG_PREFIX$floppyMediaId"

  fun extractFloppyMediaId(slug: String): String? =
    slug.takeIf { it.startsWith(SLUG_PREFIX) }?.removePrefix(SLUG_PREFIX)
}
