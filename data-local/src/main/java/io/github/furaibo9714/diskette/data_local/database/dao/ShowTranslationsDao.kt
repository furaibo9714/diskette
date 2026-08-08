package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import io.github.furaibo9714.diskette.data_local.database.model.ShowTranslation
import io.github.furaibo9714.diskette.data_local.sources.ShowTranslationsLocalDataSource

@Dao
interface ShowTranslationsDao :
  BaseDao<ShowTranslation>,
  ShowTranslationsLocalDataSource {

  @Query("SELECT * FROM shows_translations WHERE media_id == :mediaId AND language == :language")
  override suspend fun getById(
    mediaId: String,
    language: String,
  ): ShowTranslation?

  @Query("SELECT * FROM shows_translations WHERE language == :language")
  override suspend fun getAll(language: String): List<ShowTranslation>

  @Insert(onConflict = REPLACE)
  override suspend fun insertSingle(translation: ShowTranslation)

  @Query("DELETE FROM shows_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)
}
