package io.github.furaibo9714.diskette.ui_my_shows.watchlist.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class WatchlistTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  fun getLanguage() = translationsRepository.getLanguage()

  suspend fun loadTranslation(
    show: Show,
    onlyLocal: Boolean,
  ): Translation? =
    withContext(dispatchers.IO) {
      val language = getLanguage()
      if (language == Config.DEFAULT_LANGUAGE) {
        return@withContext Translation.EMPTY
      }
      translationsRepository.loadTranslation(show, language, onlyLocal)
    }
}
