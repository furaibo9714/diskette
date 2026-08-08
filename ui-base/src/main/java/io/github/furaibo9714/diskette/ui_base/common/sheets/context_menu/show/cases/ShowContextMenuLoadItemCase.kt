package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.OnHoldItemsRepository
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.RatingsRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuLoadItemCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadItem(mediaId: MediaId) =
    withContext(dispatchers.IO) {
      val show = showsRepository.detailsShow.load(mediaId)
      val language = translationsRepository.getLanguage()
      val spoilers = settingsRepository.spoilers.getAll()

      val imageAsync = async { imagesProvider.loadRemoteImage(show, ImageType.POSTER) }
      val translationAsync =
        async { translationsRepository.loadTranslation(show, language = language, onlyLocal = true) }
      val ratingAsync = async { ratingsRepository.shows.loadRatings(listOf(show)) }

      val isMyShowAsync = async { showsRepository.myShows.exists(mediaId) }
      val isWatchlistAsync = async { showsRepository.watchlistShows.exists(mediaId) }
      val isHiddenAsync = async { showsRepository.hiddenShows.exists(mediaId) }

      val isPinnedAsync = async { pinnedItemsRepository.isItemPinned(show) }
      val isOnHoldAsync = async { onHoldItemsRepository.isOnHold(show) }

      ShowContextItem(
        show = show,
        image = imageAsync.await(),
        translation = translationAsync.await(),
        userRating = ratingAsync.await().firstOrNull()?.rating,
        isMyShow = isMyShowAsync.await(),
        isWatchlist = isWatchlistAsync.await(),
        isHidden = isHiddenAsync.await(),
        isPinnedTop = isPinnedAsync.await(),
        isOnHold = isOnHoldAsync.await(),
        spoilers = spoilers,
      )
    }
}
