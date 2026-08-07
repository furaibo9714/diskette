package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.EpisodeTranslation

interface EpisodeTranslationsLocalDataSource {

  suspend fun getById(
    traktEpisodeId: Long,
    traktShowId: Long,
    language: String,
  ): EpisodeTranslation?

  suspend fun getByIds(
    traktEpisodeIds: List<Long>,
    traktShowId: Long,
    language: String,
  ): List<EpisodeTranslation>

  suspend fun insertSingle(translation: EpisodeTranslation)

  suspend fun deleteByLanguage(languages: List<String>)
}
