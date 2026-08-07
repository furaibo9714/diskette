package io.github.furaibo9714.diskette.ui_lists.details.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import io.github.furaibo9714.diskette.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ListDetailsTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  fun getLanguage() = translationsRepository.getLanguage()

  suspend fun loadTranslation(
    item: ListDetailsItem,
    onlyLocal: Boolean,
  ): Translation? =
    withContext(dispatchers.IO) {
      val language = getLanguage()
      if (language == Config.DEFAULT_LANGUAGE) {
        return@withContext Translation.EMPTY
      }
      when {
        item.isShow() -> {
          translationsRepository.loadTranslation(
            show = item.requireShow(),
            language = language,
            onlyLocal = onlyLocal,
          )
        }
        item.isMovie() -> {
          translationsRepository.loadTranslation(
            movie = item.requireMovie(),
            language = language,
            onlyLocal = onlyLocal,
          )
        }
        else -> {
          throw IllegalStateException()
        }
      }
    }
}
