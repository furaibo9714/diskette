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
  ;

  companion object {
    fun fromKey(key: String): MediaSource = entries.firstOrNull { it.key == key } ?: TMDB
  }
}

/**
 * Identity of a show or movie: the provider plus that provider's own id. Ids are opaque strings
 * because they are not all numeric - Floppy's manual entries use UUIDs.
 *
 * [key] is the single-column storage form, and the only shape written to the database or a
 * Bundle. It round-trips through [parse].
 */
@Parcelize
data class MediaId(
  val source: MediaSource,
  val id: String,
) : Parcelable {

  val key: String
    get() = "${source.key}$SEPARATOR$id"

  val isEmpty: Boolean
    get() = id.isBlank()

  /** The numeric TMDB id, or -1 when this item is not TMDB-backed. */
  val tmdbIdOrNull: Long?
    get() = if (source == MediaSource.TMDB) id.toLongOrNull() else null

  override fun toString() = key

  companion object {
    private const val SEPARATOR = ":"

    val EMPTY = MediaId(MediaSource.TMDB, "")

    fun tmdb(id: Long) = MediaId(MediaSource.TMDB, id.toString())

    fun manual(uuid: String) = MediaId(MediaSource.MANUAL, uuid)

    fun parse(key: String): MediaId {
      if (key.isBlank()) return EMPTY
      val separatorIndex = key.indexOf(SEPARATOR)
      if (separatorIndex <= 0) return MediaId(MediaSource.TMDB, key)
      return MediaId(
        source = MediaSource.fromKey(key.substring(0, separatorIndex)),
        id = key.substring(separatorIndex + 1),
      )
    }
  }
}
