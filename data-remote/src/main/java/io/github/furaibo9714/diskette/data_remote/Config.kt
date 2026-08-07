package io.github.furaibo9714.diskette.data_remote

object Config {
  const val TRAKT_DISCOVER_LIMIT = 280
  const val TRAKT_ANTICIPATED_LIMIT = 30
  const val TRAKT_RELATED_SHOWS_LIMIT = 30
  const val TRAKT_RELATED_MOVIES_LIMIT = 30
  const val TRAKT_SEARCH_LIMIT = 50

  const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
  const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
}
