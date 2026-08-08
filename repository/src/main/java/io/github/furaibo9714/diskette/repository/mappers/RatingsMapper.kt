package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.model.MovieRatings
import io.github.furaibo9714.diskette.data_local.database.model.ShowRatings
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaDetail
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Ratings
import io.github.furaibo9714.diskette.ui_model.Show
import java.util.Locale
import javax.inject.Inject

class RatingsMapper @Inject constructor() {

  fun fromNetwork(mediaDetail: FloppyMediaDetail) =
    Ratings(
      tmdb = mediaDetail.score?.let { Ratings.Value(format(it), false) },
    )

  fun fromShow(show: Show) =
    Ratings(
      tmdb = show.rating.takeIf { it > 0f }?.let { Ratings.Value(format(it.toDouble()), false) },
    )

  fun fromMovie(movie: Movie) =
    Ratings(
      tmdb = movie.rating.takeIf { it > 0f }?.let { Ratings.Value(format(it.toDouble()), false) },
    )

  private fun format(value: Double) = String.format(Locale.ENGLISH, "%.1f", value)

  fun fromDatabase(entity: MovieRatings) =
    Ratings(
      tmdb = Ratings.Value(entity.tmdb, false),
    )

  fun fromDatabase(entity: ShowRatings) =
    Ratings(
      tmdb = Ratings.Value(entity.tmdb, false),
    )

  fun toMovieDatabase(
    mediaId: MediaId,
    ratings: Ratings,
  ) = MovieRatings(
    id = 0,
    mediaId = mediaId.key,
    tmdb = ratings.tmdb?.value,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )

  fun toShowDatabase(
    mediaId: MediaId,
    ratings: Ratings,
  ) = ShowRatings(
    id = 0,
    mediaId = mediaId.key,
    tmdb = ratings.tmdb?.value,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )
}
