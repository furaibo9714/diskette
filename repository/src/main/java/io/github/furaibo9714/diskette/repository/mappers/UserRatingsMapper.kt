package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.UserRating
import java.time.ZonedDateTime
import javax.inject.Inject

class UserRatingsMapper @Inject constructor() {

  fun fromDatabase(entity: Rating) =
    UserRating(
      mediaId = MediaId.parse(entity.mediaId),
      rating = entity.rating,
      ratedAt = entity.ratedAt,
    )

  fun toDatabaseMovie(
    movie: Movie,
    rating: Int,
    ratedAt: ZonedDateTime,
  ) = Rating(
    mediaId = movie.mediaId.key,
    type = "movie",
    rating = rating,
    seasonNumber = null,
    episodeNumber = null,
    ratedAt = ratedAt,
    createdAt = nowUtc(),
    updatedAt = nowUtc(),
  )

  fun toDatabaseShow(
    show: Show,
    rating: Int,
    ratedAt: ZonedDateTime,
  ) = Rating(
    mediaId = show.mediaId.key,
    type = "show",
    rating = rating,
    seasonNumber = null,
    episodeNumber = null,
    ratedAt = ratedAt,
    createdAt = nowUtc(),
    updatedAt = nowUtc(),
  )

  fun toDatabaseEpisode(
    episode: Episode,
    rating: Int,
    ratedAt: ZonedDateTime,
  ) = Rating(
    mediaId = episode.ids.media.key,
    type = "episode",
    rating = rating,
    seasonNumber = episode.season,
    episodeNumber = episode.number,
    ratedAt = ratedAt,
    createdAt = nowUtc(),
    updatedAt = nowUtc(),
  )

  fun toDatabaseSeason(
    season: Season,
    rating: Int,
    ratedAt: ZonedDateTime,
  ) = Rating(
    mediaId = season.ids.media.key,
    type = "season",
    rating = rating,
    seasonNumber = season.number,
    episodeNumber = null,
    ratedAt = ratedAt,
    createdAt = nowUtc(),
    updatedAt = nowUtc(),
  )
}
