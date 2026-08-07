package io.github.furaibo9714.diskette.data_remote.tmdb

/**
 * Trakt's API is paywalled and no longer usable as a metadata source, but the whole app still
 * keys identity off `IdTrakt`/`id_trakt` (Room PKs, sync queues, DiffCallback identity, etc.).
 * Renaming that type across the app is out of scope, so TMDB-sourced content instead gets a
 * synthetic `id_trakt` value: this offset + the real TMDB id. The offset is chosen safely above
 * any historical real Trakt id, so synthetic ids can never collide with a row that still carries
 * a real legacy Trakt id. Real TMDB API calls, images, and Floppy sync all continue to use the
 * separate, unmodified `ids.tmdb`/`id_tmdb` column - this offset only ever touches `ids.trakt`.
 */
object TmdbSyntheticIds {

  const val OFFSET = 1_000_000_000L

  fun toSyntheticTraktId(tmdbId: Long): Long = OFFSET + tmdbId

  fun isSynthetic(traktId: Long): Boolean = traktId >= OFFSET

  fun toTmdbId(traktId: Long): Long = traktId - OFFSET
}
