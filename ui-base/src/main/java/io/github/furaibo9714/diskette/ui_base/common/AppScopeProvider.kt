package io.github.furaibo9714.diskette.ui_base.common

import kotlinx.coroutines.CoroutineScope

interface AppScopeProvider {
  val appScope: CoroutineScope
}
