package io.github.furaibo9714.diskette.ui.main

import io.github.furaibo9714.diskette.utilities.deeplink.DeepLinkBundle
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_settings.helpers.AppLanguage

// TODO Split events into their Channel
data class MainUiState(
  val isLoading: Boolean = false,
  val isInitialRun: Event<Boolean>? = null,
  val showWhatsNew: Event<Boolean>? = null,
  val initialLanguage: Event<AppLanguage>? = null,
  val showMask: Boolean = false,
  val openLink: Event<DeepLinkBundle>? = null,
)
