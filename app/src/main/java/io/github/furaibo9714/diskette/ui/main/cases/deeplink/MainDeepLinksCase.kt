package io.github.furaibo9714.diskette.ui.main.cases.deeplink

import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainDeepLinksCase @Inject constructor(
  private val imdbDeepLinkCase: ImdbDeepLinkCase,
  private val tmdbDeepLinkCase: TmdbDeepLinkCase,
) {

  suspend fun findById(imdbId: IdImdb) = imdbDeepLinkCase.findById(imdbId)

  suspend fun findById(
    tmdbId: IdTmdb,
    type: String,
  ) = tmdbDeepLinkCase.findById(tmdbId, type)
}
