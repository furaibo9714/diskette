package io.github.furaibo9714.diskette.repository

import android.content.SharedPreferences
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnHoldItemsRepository @Inject constructor(
  @Named("progressOnHoldPreferences") private val sharedPreferences: SharedPreferences,
) {

  fun getAll(): List<MediaId> = sharedPreferences.all.keys.map { MediaId.parse(it.toLong()) }

  fun addItem(show: Show) = addItem(MediaId.parse(show.mediaId.key))

  fun addItem(showId: MediaId) = sharedPreferences.edit().putLong(showId.id.toString(), showId.id).apply()

  fun removeItem(show: Show) = sharedPreferences.edit().remove(show.mediaId.toString()).apply()

  fun isOnHold(show: Show) = sharedPreferences.contains(show.mediaId.toString())
}
