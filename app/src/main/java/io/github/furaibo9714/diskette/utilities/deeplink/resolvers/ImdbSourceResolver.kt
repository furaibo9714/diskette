package io.github.furaibo9714.diskette.utilities.deeplink.resolvers

import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkSource
import io.github.furaibo9714.diskette.ui_model.IdImdb

class ImdbSourceResolver : SourceResolver {

  override fun resolve(linkPath: List<String>): DeepLinkSource? {
    if (linkPath.size < 2 || !linkPath[1].startsWith("tt") || linkPath[1].length <= 2) {
      return null
    }

    return DeepLinkSource.ImdbSource(IdImdb(linkPath[1]))
  }
}
