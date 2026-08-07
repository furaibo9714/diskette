@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_lists.details

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

sealed class ListDetailsUiEvent<T>(
  action: T,
) : Event<T>(action)
