package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.model.Movie as MovieDb
import io.github.furaibo9714.diskette.data_remote.media.model.Movie as MovieNetwork
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.MovieStatus
import java.time.LocalDate
import javax.inject.Inject

class MovieMapper @Inject constructor(
  private val idsMapper: IdsMapper,
) {

  fun fromNetwork(movie: MovieNetwork) =
    Movie(
      idsMapper.fromNetwork(movie.ids),
      movie.title ?: "",
      movie.year ?: -1,
      movie.overview ?: "",
      movie.released?.let { if (it.isNotBlank()) LocalDate.parse(it) else null },
      movie.runtime ?: -1,
      movie.country ?: "",
      movie.trailer ?: "",
      movie.homepage ?: "",
      movie.language ?: "",
      MovieStatus.fromKey(movie.status),
      movie.rating ?: -1F,
      movie.votes ?: -1,
      movie.comment_count ?: -1,
      movie.genres ?: emptyList(),
      nowUtcMillis(),
      nowUtcMillis(),
    )

  fun toNetwork(movie: Movie) =
    MovieNetwork(
      idsMapper.toNetwork(movie.ids),
      movie.title,
      movie.year,
      movie.overview,
      movie.released?.toString(),
      movie.runtime,
      movie.country,
      movie.trailer,
      movie.homepage,
      movie.status.key,
      movie.rating,
      movie.votes,
      movie.commentCount,
      movie.genres,
      movie.language,
    )

  fun fromDatabase(movie: MovieDb) =
    Movie(
      idsMapper.fromDatabase(movie),
      movie.title,
      movie.year,
      movie.overview,
      if (movie.released.isBlank()) null else LocalDate.parse(movie.released),
      movie.runtime,
      movie.country,
      movie.trailer,
      movie.homepage,
      movie.language,
      MovieStatus.fromKey(movie.status),
      movie.rating,
      movie.votes,
      movie.commentCount,
      movie.genres.split(","),
      movie.updatedAt,
      movie.createdAt,
    )

  fun toDatabase(movie: Movie) =
    MovieDb(
      movie.ids.trakt.id,
      movie.ids.tmdb.id,
      movie.ids.imdb.id,
      movie.ids.slug.id,
      movie.title,
      movie.year,
      movie.overview,
      movie.released?.toString() ?: "",
      movie.runtime,
      movie.country,
      movie.trailer,
      movie.language,
      movie.homepage,
      movie.status.key,
      movie.rating,
      movie.votes,
      movie.commentCount,
      movie.genres.joinToString(","),
      nowUtcMillis(),
      movie.createdAt,
    )
}
