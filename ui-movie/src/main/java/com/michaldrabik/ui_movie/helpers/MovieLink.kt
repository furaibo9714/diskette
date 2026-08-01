package com.michaldrabik.ui_movie.helpers

enum class MovieLink {
  TMDB,
  ;

  fun getUri(id: String) =
    when (this) {
      TMDB -> {
        "https://www.themoviedb.org/movie/$id"
      }
    }
}
