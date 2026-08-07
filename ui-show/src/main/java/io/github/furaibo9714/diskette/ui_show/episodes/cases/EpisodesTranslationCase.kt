package io.github.furaibo9714.diskette.ui_show.episodes.cases

import io.github.furaibo9714.diskette.common.Config.DEFAULT_LANGUAGE
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.ui_model.Season
import io.github.furaibo9714.diskette.ui_model.SeasonTranslation
import io.github.furaibo9714.diskette.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesTranslationCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun loadTranslations(
    season: Season?,
    show: Show,
  ): List<SeasonTranslation> =
    withContext(dispatchers.IO) {
      if (season == null) {
        return@withContext emptyList()
      }

      val language = translationsRepository.getLanguage()
      if (language == DEFAULT_LANGUAGE) {
        return@withContext emptyList()
      }

      translationsRepository.loadTranslations(season, show.ids.trakt, language)
    }
}
