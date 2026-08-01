package com.michaldrabik.ui_show.sections.ratings

enum class ShowLink {
  TMDB,
  ;

  fun getUri(id: String) =
    when (this) {
      TMDB -> {
        "https://www.themoviedb.org/tv/$id"
      }
    }
}
