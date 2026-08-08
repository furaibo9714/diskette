package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.furaibo9714.diskette.data_local.database.model.EpisodeTranslation
import io.github.furaibo9714.diskette.data_local.sources.EpisodeTranslationsLocalDataSource

@Dao
interface EpisodeTranslationsDao :
  BaseDao<EpisodeTranslation>,
  EpisodeTranslationsLocalDataSource {

  @Query(
    "SELECT * FROM episodes_translations " +
      "WHERE media_id == :episodeMediaId AND show_media_id == :showMediaId AND language == :language",
  )
  override suspend fun getById(
    episodeMediaId: String,
    showMediaId: String,
    language: String,
  ): EpisodeTranslation?

  @Query(
    "SELECT * FROM episodes_translations " +
      "WHERE media_id IN (:episodeMediaIds) AND show_media_id == :showMediaId AND language == :language",
  )
  override suspend fun getByIds(
    episodeMediaIds: List<String>,
    showMediaId: String,
    language: String,
  ): List<EpisodeTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insertSingle(translation: EpisodeTranslation)

  @Query("DELETE FROM episodes_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)
}
