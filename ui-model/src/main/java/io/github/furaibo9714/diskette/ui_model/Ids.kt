package io.github.furaibo9714.diskette.ui_model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [media] is the item's identity - everything else is a cross-reference kept for links, images
 * and lookups.
 */
@Parcelize
data class Ids(
  val media: MediaId,
  val slug: IdSlug,
  val tvdb: IdTvdb,
  val imdb: IdImdb,
  val tmdb: IdTmdb,
) : Parcelable {

  companion object {
    val EMPTY = Ids(
      MediaId.EMPTY,
      IdSlug(),
      IdTvdb(),
      IdImdb(),
      IdTmdb(),
    )

    /** Builds the ids of a TMDB-backed item, whose identity and TMDB cross-reference match. */
    fun tmdb(tmdbId: Long) = EMPTY.copy(media = MediaId.tmdb(tmdbId), tmdb = IdTmdb(tmdbId))
  }
}

sealed interface Id : Parcelable

@JvmInline
@Parcelize
value class IdTvdb(
  val id: Long = -1,
) : Id

@JvmInline
@Parcelize
value class IdImdb(
  val id: String = "",
) : Id

@JvmInline
@Parcelize
value class IdTmdb(
  val id: Long = -1,
) : Id

@JvmInline
@Parcelize
value class IdSlug(
  val id: String = "",
) : Id
