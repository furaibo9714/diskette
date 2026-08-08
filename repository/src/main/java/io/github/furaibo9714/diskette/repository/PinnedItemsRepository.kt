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

  fun addPinnedItem(show: Show) = addShowPinnedItem(MediaId.parse(show.mediaId.key))

  fun addPinnedItem(movie: Movie) = addMoviePinnedItem(MediaId.parse(movie.mediaId.key))

  fun addShowPinnedItem(showId: MediaId) = sharedPreferences.edit().putLong(showId.id.toString(), showId.id).apply()

  fun addMoviePinnedItem(movieId: MediaId) =
    sharedPreferencesMovies.edit().putLong(movieId.id.toString(), movieId.id).apply()

  fun removePinnedItem(show: Show) = sharedPreferences.edit().remove(show.mediaId.toString()).apply()

  fun removePinnedItem(movie: Movie) = sharedPreferencesMovies.edit().remove(movie.mediaId.toString()).apply()

  fun isItemPinned(show: Show) = sharedPreferences.contains(show.mediaId.toString())

  fun isItemPinned(movie: Movie) = sharedPreferencesMovies.contains(movie.mediaId.toString())

  fun getAllMovies(): List<Long> = sharedPreferencesMovies.all.values.map { it as Long }

  fun getAllShows(): List<Long> = sharedPreferences.all.values.map { it as Long }
}
