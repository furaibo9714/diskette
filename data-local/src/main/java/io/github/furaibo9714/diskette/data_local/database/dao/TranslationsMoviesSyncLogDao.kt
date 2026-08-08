package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.furaibo9714.diskette.data_local.database.model.TranslationsMoviesSyncLog
import io.github.furaibo9714.diskette.data_local.sources.TranslationsMoviesSyncLogLocalDataSource

@Dao
interface TranslationsMoviesSyncLogDao : TranslationsMoviesSyncLogLocalDataSource {

  @Query("SELECT * from sync_movies_translations_log")
  override suspend fun getAll(): List<TranslationsMoviesSyncLog>

  @Query("SELECT * from sync_movies_translations_log WHERE movie_media_id == :mediaId")
  override suspend fun getById(mediaId: String): TranslationsMoviesSyncLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(log: TranslationsMoviesSyncLog)

  @Query("DELETE FROM sync_movies_translations_log")
  override suspend fun deleteAll()
}
