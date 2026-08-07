@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_settings.sections.notifications

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

internal sealed class SettingsNotificationsUiEvent<T>(
  action: T,
) : Event<T>(action) {
  data object RequestNotificationsPermission : SettingsNotificationsUiEvent<Unit>(Unit)
}
