package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.database.model.Show as ShowDb
import io.github.furaibo9714.diskette.data_remote.media.model.AirTime as AirTimeNetwork
import io.github.furaibo9714.diskette.data_remote.media.model.Show as ShowNetwork
import io.github.furaibo9714.diskette.ui_model.AirTime
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.ShowStatus
import javax.inject.Inject

class ShowMapper @Inject constructor(
  private val idsMapper: IdsMapper,
) {

  fun fromNetwork(show: ShowNetwork) =
    Show(
      idsMapper.fromNetwork(show.ids),
      show.title ?: "",
      show.year ?: -1,
      show.overview ?: "",
      show.first_aired ?: "",
      show.runtime ?: -1,
      AirTime(
        show.airs?.day ?: "",
        show.airs?.time ?: "",
        show.airs?.timezone ?: "",
      ),
      show.certification ?: "",
      show.network ?: "",
      show.country ?: "",
      show.trailer ?: "",
      show.homepage ?: "",
      ShowStatus.fromKey(show.status),
      show.rating ?: -1F,
      show.votes ?: -1,
      show.comment_count ?: -1,
      show.genres ?: emptyList(),
      show.aired_episodes ?: -1,
      nowUtcMillis(),
      nowUtcMillis(),
      show.runtime_max ?: -1,
    )

  fun toNetwork(show: Show) =
    ShowNetwork(
      idsMapper.toNetwork(show.ids),
      show.title,
      show.year,
      show.overview,
      show.firstAired,
      show.runtime,
      AirTimeNetwork(
        show.airTime.day,
        show.airTime.time,
        show.airTime.timezone,
      ),
      show.certification,
      show.network,
      show.country,
      show.trailer,
      show.homepage,
      show.status.key,
      show.rating,
      show.votes,
      show.commentCount,
      show.genres,
      show.airedEpisodes,
      runtime_max = show.runtimeMax.takeIf { it > 0 },
    )

  fun fromDatabase(show: ShowDb) =
    Show(
      idsMapper.fromDatabase(show),
      show.title,
      show.year,
      show.overview,
      show.firstAired,
      show.runtime,
      AirTime(show.airtimeDay, show.airtimeTime, show.airtimeTimezone),
      show.certification,
      show.network,
      show.country,
      show.trailer,
      show.homepage,
      ShowStatus.fromKey(show.status),
      show.rating,
      show.votes,
      show.commentCount,
      show.genres.split(","),
      show.airedEpisodes,
      show.createdAt,
      show.updatedAt,
      show.runtimeMax,
    )

  fun toDatabase(show: Show) =
    ShowDb(
      show.traktId,
      show.ids.tvdb.id,
      show.ids.tmdb.id,
      show.ids.imdb.id,
      show.ids.slug.id,
      show.ids.tvrage.id,
      show.title,
      show.year,
      show.overview,
      show.firstAired,
      show.runtime,
      show.airTime.day,
      show.airTime.time,
      show.airTime.timezone,
      show.certification,
      show.network,
      show.country,
      show.trailer,
      show.homepage,
      show.status.key,
      show.rating,
      show.votes,
      show.commentCount,
      show.genres.joinToString(","),
      show.airedEpisodes,
      show.createdAt,
      nowUtcMillis(),
      show.runtimeMax,
    )
}
