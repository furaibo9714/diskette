package io.github.furaibo9714.diskette.ui_my_movies.main

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

sealed class FollowedMoviesUiEvent<T>(
  action: T,
) : Event<T>(action)
