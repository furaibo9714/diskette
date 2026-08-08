package io.github.furaibo9714.diskette.ui_show.sections.nextepisode.cases

import io.github.furaibo9714.diskette.common.Config.DEFAULT_LANGUAGE
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.ui_model.Episode
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsTranslationCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun loadTranslation(
    episode: Episode,
    show: Show,
    onlyLocal: Boolean = false,
  ): Translation? =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      if (language == DEFAULT_LANGUAGE) {
        return@withContext null
      }
      translationsRepository.loadTranslation(episode, show.ids.media, language, onlyLocal)
    }
}
