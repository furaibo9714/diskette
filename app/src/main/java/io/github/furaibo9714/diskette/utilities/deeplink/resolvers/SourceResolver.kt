package io.github.furaibo9714.diskette.utilities.deeplink.resolvers

import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkSource

interface SourceResolver {
  fun resolve(linkPath: List<String>): DeepLinkSource?
}
