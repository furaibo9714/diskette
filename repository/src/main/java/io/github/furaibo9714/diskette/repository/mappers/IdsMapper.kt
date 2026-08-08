package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.IdTvdb
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.MediaId
import javax.inject.Inject
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb
import io.github.furaibo9714.diskette.data_remote.media.model.Ids as IdsNetwork

class IdsMapper @Inject constructor() {

  /** Everything the remote source returns is TMDB-backed, so identity comes from the TMDB id. */
  fun fromNetwork(ids: IdsNetwork?) =
    Ids(
      media = ids?.tmdb?.let { MediaId.tmdb(it) } ?: MediaId.EMPTY,
      slug = IdSlug(ids?.slug ?: ""),
      tvdb = IdTvdb(ids?.tvdb ?: -1),
      imdb = IdImdb(ids?.imdb ?: ""),
      tmdb = IdTmdb(ids?.tmdb ?: -1),
    )

  fun toNetwork(ids: Ids?) =
    IdsNetwork(
      slug = ids?.slug?.id,
      tvdb = ids?.tvdb?.id,
      imdb = ids?.imdb?.id,
      tmdb = ids?.tmdb?.id,
    )

  fun fromDatabase(show: ShowDb?) =
    Ids(
      media = MediaId.parse(show?.mediaId ?: ""),
      slug = IdSlug(show?.idSlug ?: ""),
      tvdb = IdTvdb(show?.idTvdb ?: -1),
      imdb = IdImdb(show?.idImdb ?: ""),
      tmdb = IdTmdb(show?.idTmdb ?: -1),
    )

  fun fromDatabase(movie: Movie?) =
    Ids(
      media = MediaId.parse(movie?.mediaId ?: ""),
      slug = IdSlug(movie?.idSlug ?: ""),
      tvdb = IdTvdb(-1),
      imdb = IdImdb(movie?.idImdb ?: ""),
      tmdb = IdTmdb(movie?.idTmdb ?: -1),
    )
}
