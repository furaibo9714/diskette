package io.github.furaibo9714.diskette.ui_settings.sections.general.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.movies.MovieStreamingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowStreamingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsGeneralStreamingsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showStreamingsRepository: ShowStreamingsRepository,
  private val movieStreamingsRepository: MovieStreamingsRepository,
) {

  suspend fun deleteCache() =
    withContext(dispatchers.IO) {
      showStreamingsRepository.deleteCache()
      movieStreamingsRepository.deleteCache()
    }
}
