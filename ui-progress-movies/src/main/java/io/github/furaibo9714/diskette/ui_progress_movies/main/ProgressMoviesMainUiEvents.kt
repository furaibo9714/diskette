@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_progress_movies.main

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.ProgressDateSelectionType

data class MovieCheckActionUiEvent(
  val movie: Movie,
  val dateSelectionType: ProgressDateSelectionType,
) : Event<Movie>(movie)

object RequestWidgetsUpdate : Event<Unit>(Unit)
