package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.model.MovieRatings
import io.github.furaibo9714.diskette.data_local.database.model.ShowRatings
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaDetail
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Ratings
import java.util.Locale
import javax.inject.Inject

class RatingsMapper @Inject constructor() {

  fun fromNetwork(mediaDetail: FloppyMediaDetail) =
    Ratings(
      tmdb = mediaDetail.score?.let { Ratings.Value(String.format(Locale.ENGLISH, "%.1f", it), false) },
    )

  fun fromDatabase(entity: MovieRatings) =
    Ratings(
      tmdb = Ratings.Value(entity.tmdb, false),
    )

  fun fromDatabase(entity: ShowRatings) =
    Ratings(
      tmdb = Ratings.Value(entity.tmdb, false),
    )

  fun toMovieDatabase(
    idTrakt: IdTrakt,
    ratings: Ratings,
  ) = MovieRatings(
    id = 0,
    idTrakt = idTrakt.id,
    tmdb = ratings.tmdb?.value,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )

  fun toShowDatabase(
    idTrakt: IdTrakt,
    ratings: Ratings,
  ) = ShowRatings(
    id = 0,
    idTrakt = idTrakt.id,
    tmdb = ratings.tmdb?.value,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )
}
