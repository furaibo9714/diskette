package io.github.furaibo9714.diskette.data_remote

object Config {
  const val DISCOVER_LIMIT = 280
  const val ANTICIPATED_LIMIT = 30
  const val RELATED_SHOWS_LIMIT = 30
  const val RELATED_MOVIES_LIMIT = 30
  const val SEARCH_LIMIT = 50

  const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
  const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
}
