package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.Season as SeasonDb
import io.github.furaibo9714.diskette.data_remote.media.model.Season as SeasonNetwork
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Season
import java.time.ZonedDateTime
import javax.inject.Inject

class SeasonMapper @Inject constructor(
  private val idsMapper: IdsMapper,
  private val episodeMapper: EpisodeMapper,
) {

  fun fromNetwork(season: SeasonNetwork) =
    Season(
      idsMapper.fromNetwork(season.ids),
      season.number ?: -1,
      season.episode_count ?: -1,
      season.aired_episodes ?: -1,
      season.title ?: "",
      if (season.first_aired.isNullOrBlank()) null else ZonedDateTime.parse(season.first_aired),
      season.overview ?: "",
      season.rating ?: -1F,
      season.episodes?.map { episodeMapper.fromNetwork(it) } ?: emptyList(),
    )

  fun toNetwork(season: Season) =
    SeasonNetwork(
      ids = idsMapper.toNetwork(season.ids),
      number = season.number,
      episode_count = season.episodeCount,
      aired_episodes = season.airedEpisodes,
      title = season.title,
      first_aired = season.firstAired.toString(),
      overview = season.overview,
      rating = season.rating,
      episodes = season.episodes.map { episodeMapper.toNetwork(it) },
    )

  fun fromDatabase(
    seasonDb: SeasonDb,
    episodes: List<Episode> = emptyList(),
  ) = Season(
    Ids.EMPTY.copy(media = MediaId.parse(seasonDb.mediaId)),
    seasonDb.seasonNumber,
    seasonDb.episodesCount,
    seasonDb.episodesAiredCount,
    seasonDb.seasonTitle,
    seasonDb.seasonFirstAired,
    seasonDb.seasonOverview,
    seasonDb.rating ?: -1F,
    episodes.map { episodeMapper.fromDatabase(it) },
  )

  fun toDatabase(
    season: Season,
    showId: MediaId,
    isWatched: Boolean,
  ): SeasonDb =
    SeasonDb(
      season.ids.media.key,
      showId.key,
      season.number,
      season.title,
      season.overview,
      season.firstAired,
      season.episodeCount,
      season.airedEpisodes,
      season.rating,
      isWatched,
    )
}
