package io.github.furaibo9714.diskette.ui_lists.details.cases

import android.content.SharedPreferences
import io.github.furaibo9714.diskette.ui_model.Tip
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import javax.inject.Named

@ViewModelScoped
class ListDetailsTipsCase @Inject constructor(
  @Named("tipsPreferences") private val sharedPreferences: SharedPreferences,
) {

  fun isTipShown(tip: Tip) = sharedPreferences.getBoolean(tip.name, false)

  fun setTipShown(tip: Tip) {
    sharedPreferences.edit().putBoolean(tip.name, true).apply()
  }
}
