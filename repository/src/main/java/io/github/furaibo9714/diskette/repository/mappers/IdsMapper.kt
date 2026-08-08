package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb
import io.github.furaibo9714.diskette.data_remote.media.model.Ids as IdsNetwork
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.IdTvRage
import io.github.furaibo9714.diskette.ui_model.IdTvdb
import io.github.furaibo9714.diskette.ui_model.Ids
import javax.inject.Inject

class IdsMapper @Inject constructor() {

  fun fromNetwork(ids: IdsNetwork?) =
    Ids(
      IdTrakt(ids?.trakt ?: -1),
      IdSlug(ids?.slug ?: ""),
      IdTvdb(ids?.tvdb ?: -1),
      IdImdb(ids?.imdb ?: ""),
      IdTmdb(ids?.tmdb ?: -1),
      IdTvRage(ids?.tvrage ?: -1),
    )

  fun toNetwork(ids: Ids?) =
    IdsNetwork(
      trakt = ids?.trakt?.id,
      slug = ids?.slug?.id,
      tvdb = ids?.tvdb?.id,
      imdb = ids?.imdb?.id,
      tmdb = ids?.tmdb?.id,
      tvrage = ids?.tvrage?.id,
    )

  fun fromDatabase(show: ShowDb?) =
    Ids(
      IdTrakt(show?.idTrakt ?: -1),
      IdSlug(show?.idSlug ?: ""),
      IdTvdb(show?.idTvdb ?: -1),
      IdImdb(show?.idImdb ?: ""),
      IdTmdb(show?.idTmdb ?: -1),
      IdTvRage(show?.idTvrage ?: -1),
    )

  fun fromDatabase(movie: Movie?) =
    Ids(
      IdTrakt(movie?.idTrakt ?: -1),
      IdSlug(movie?.idSlug ?: ""),
      IdTvdb(-1),
      IdImdb(movie?.idImdb ?: ""),
      IdTmdb(movie?.idTmdb ?: -1),
      IdTvRage(-1),
    )
}
