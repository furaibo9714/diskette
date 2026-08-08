package io.github.furaibo9714.diskette.ui_model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The provider that owns an item's id. Mirrors Floppy's own `source` path segment, so a
 * [MediaId] addresses an item on Floppy directly: `/api/v1/media/{type}/{source}/{id}/`.
 */
enum class MediaSource(
  val key: String,
) {
  TMDB("tmdb"),
  TVDB("tvdb"),
  MAL("mal"),

  /** Items tracked in Floppy with no external provider id, identified by a Floppy-issued UUID. */
  MANUAL("manual"),

  /**
   * An id the app derived itself because no provider named the row. Only rows that are addressed
   * some other way - seasons and episodes, which Floppy reaches through the show plus their
   * numbers - can carry one, so a [LOCAL] id is never sent anywhere.
   */
  LOCAL("local"),
  ;

  companion object {
    fun fromKeyOrNull(key: String): MediaSource? = entries.firstOrNull { it.key == key }

    fun fromKey(key: String): MediaSource = fromKeyOrNull(key) ?: TMDB
  }
}

/**
 * Identity of a show or movie: the provider plus that provider's own id. Ids are opaque strings
 * because they are not all numeric - Floppy's manual entries use UUIDs.
 *
 * The two halves are deliberately named apart. [key] is the whole identity and the only shape ever
 * written to the database or a Bundle; [providerId] is one half of it and means nothing without
 * its [source], so it belongs only in a Floppy request path. Reaching for [providerId] where [key]
 * was meant would look up "1396" in a table keyed "tmdb:1396" and silently find nothing.
 */
@Parcelize
data class MediaId(
  val source: MediaSource,
  val providerId: String,
) : Parcelable {

  /** The storage and transport form. Round-trips through [parse]. */
  val key: String
    get() = "${source.key}$SEPARATOR$providerId"

  val isEmpty: Boolean
    get() = providerId.isBlank()

  /** The numeric TMDB id, or null when this item is not TMDB-backed. */
  val tmdbIdOrNull: Long?
    get() = if (source == MediaSource.TMDB) providerId.toLongOrNull() else null

  /**
   * A stable number derived from [key], for the Android APIs that insist on a numeric row id -
   * `RemoteViewsFactory.getItemId` and PendingIntent request codes. It is not an identity: two
   * items can collide, so nothing may be stored under it or addressed by it.
   */
  val stableLongId: Long
    get() = key.hashCode().toLong()

  override fun toString() = key

  companion object {
    private const val SEPARATOR = ":"

    val EMPTY = MediaId(MediaSource.TMDB, "")

    fun tmdb(id: Long) = MediaId(MediaSource.TMDB, id.toString())

    fun manual(uuid: String) = MediaId(MediaSource.MANUAL, uuid)

    fun local(id: String) = MediaId(MediaSource.LOCAL, id)

    fun parse(key: String): MediaId {
      if (key.isBlank()) return EMPTY
      val separatorIndex = key.indexOf(SEPARATOR)
      if (separatorIndex <= 0) return MediaId(MediaSource.TMDB, key)
      return MediaId(
        source = MediaSource.fromKey(key.substring(0, separatorIndex)),
        providerId = key.substring(separatorIndex + 1),
      )
    }
  }
}
