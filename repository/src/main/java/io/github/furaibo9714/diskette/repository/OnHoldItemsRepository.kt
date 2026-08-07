package io.github.furaibo9714.diskette.repository

import android.content.SharedPreferences
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnHoldItemsRepository @Inject constructor(
  @Named("progressOnHoldPreferences") private val sharedPreferences: SharedPreferences,
) {

  fun getAll(): List<IdTrakt> = sharedPreferences.all.keys.map { IdTrakt(it.toLong()) }

  fun addItem(show: Show) = addItem(IdTrakt(show.traktId))

  fun addItem(showId: IdTrakt) = sharedPreferences.edit().putLong(showId.id.toString(), showId.id).apply()

  fun removeItem(show: Show) = sharedPreferences.edit().remove(show.traktId.toString()).apply()

  fun isOnHold(show: Show) = sharedPreferences.contains(show.traktId.toString())
}
