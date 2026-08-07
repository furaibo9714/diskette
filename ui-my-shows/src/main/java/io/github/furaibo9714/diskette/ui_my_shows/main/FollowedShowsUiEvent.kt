package io.github.furaibo9714.diskette.ui_my_shows.main

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

sealed class FollowedShowsUiEvent<T>(
  action: T,
) : Event<T>(action)
