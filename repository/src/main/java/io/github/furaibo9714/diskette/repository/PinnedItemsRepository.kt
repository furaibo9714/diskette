package io.github.furaibo9714.diskette.repository

import android.content.SharedPreferences
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PinnedItemsRepository @Inject constructor(
  @Named("watchlistPreferences") private val sharedPreferences: SharedPreferences,
  @Named("progressMoviesPreferences") private val sharedPreferencesMovies: SharedPreferences,
) {

  fun addPinnedItem(show: Show) = addShowPinnedItem(show.mediaId)

  fun addPinnedItem(movie: Movie) = addMoviePinnedItem(movie.mediaId)

  fun addShowPinnedItem(showId: MediaId) = sharedPreferences.edit().putString(showId.key, showId.key).apply()

  fun addMoviePinnedItem(movieId: MediaId) =
    sharedPreferencesMovies.edit().putString(movieId.key, movieId.key).apply()

  fun removePinnedItem(show: Show) = sharedPreferences.edit().remove(show.mediaId.key).apply()

  fun removePinnedItem(movie: Movie) = sharedPreferencesMovies.edit().remove(movie.mediaId.key).apply()

  fun isItemPinned(show: Show) = sharedPreferences.contains(show.mediaId.key)

  fun isItemPinned(movie: Movie) = sharedPreferencesMovies.contains(movie.mediaId.key)

  fun getAllMovies(): List<MediaId> = sharedPreferencesMovies.all.keys.map { MediaId.parse(it) }

  fun getAllShows(): List<MediaId> = sharedPreferences.all.keys.map { MediaId.parse(it) }
}
