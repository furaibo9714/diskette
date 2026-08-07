package io.github.furaibo9714.diskette.data_remote.tmdb

/**
 * Maps TMDB's numeric genre ids to the Trakt-style slugs `ui_model.Genre.fromSlug()` already
 * understands, so genre chips/filters keep working unchanged. TMDB's TV and Movie genre sets are
 * different from each other and don't line up 1:1 with Trakt's (e.g. TV's combined "Action &
 * Adventure"/"Sci-Fi & Fantasy" have no split equivalent) - unmapped/ambiguous ids are simply
 * omitted rather than forced into a misleading slug; `Genre.ANIME` has no TMDB equivalent at all.
 */
object TmdbGenreSlugs {

  private val MOVIE = mapOf(
    28 to "action",
    12 to "adventure",
    16 to "animation",
    35 to "comedy",
    80 to "crime",
    99 to "documentary",
    18 to "drama",
    14 to "fantasy",
    36 to "history",
    27 to "horror",
    10749 to "romance",
    878 to "science-fiction",
    53 to "thriller",
    10752 to "war",
    37 to "western",
  )

  private val TV = mapOf(
    10759 to "action",
    16 to "animation",
    35 to "comedy",
    80 to "crime",
    99 to "documentary",
    18 to "drama",
    10765 to "science-fiction",
    10768 to "war",
    37 to "western",
  )

  fun movieSlugs(genreIds: List<Int>?): List<String> = genreIds.orEmpty().mapNotNull { MOVIE[it] }

  fun tvSlugs(genreIds: List<Int>?): List<String> = genreIds.orEmpty().mapNotNull { TV[it] }

  fun movieGenreIds(slugs: List<String>): List<Int> {
    val bySlug = MOVIE.entries.associate { (id, slug) -> slug to id }
    return slugs.mapNotNull { bySlug[it] }
  }

  fun tvGenreIds(slugs: List<String>): List<Int> {
    val bySlug = TV.entries.associate { (id, slug) -> slug to id }
    return slugs.mapNotNull { bySlug[it] }
  }
}
