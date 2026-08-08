package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.ShowTranslation

interface ShowTranslationsLocalDataSource {

  suspend fun getById(
    mediaId: String,
    language: String,
  ): ShowTranslation?

  suspend fun getAll(language: String): List<ShowTranslation>

  suspend fun insertSingle(translation: ShowTranslation)

  suspend fun deleteByLanguage(languages: List<String>)
}
