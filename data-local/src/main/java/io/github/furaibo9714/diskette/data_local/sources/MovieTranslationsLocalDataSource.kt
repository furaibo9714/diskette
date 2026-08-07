package io.github.furaibo9714.diskette.data_local.sources

import io.github.furaibo9714.diskette.data_local.database.model.MovieTranslation

interface MovieTranslationsLocalDataSource {

  suspend fun getById(
    traktId: Long,
    language: String,
  ): MovieTranslation?

  suspend fun getAll(language: String): List<MovieTranslation>

  suspend fun insertSingle(translation: MovieTranslation)

  suspend fun deleteByLanguage(languages: List<String>)
}
