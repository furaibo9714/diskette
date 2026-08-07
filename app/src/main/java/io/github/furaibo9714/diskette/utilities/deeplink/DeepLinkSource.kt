package io.github.furaibo9714.diskette.utilities.deeplink

import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdTmdb

sealed class DeepLinkSource {

  data class ImdbSource(
    val id: IdImdb,
  ) : DeepLinkSource()

  data class TmdbSource(
    val id: IdTmdb,
    val type: String,
  ) : DeepLinkSource()
}
