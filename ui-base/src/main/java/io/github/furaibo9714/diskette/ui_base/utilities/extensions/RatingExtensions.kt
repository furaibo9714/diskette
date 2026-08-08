package io.github.furaibo9714.diskette.ui_base.utilities.extensions

import android.view.View
import android.widget.TextView
import io.github.furaibo9714.diskette.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import java.util.Locale.ENGLISH

/**
 * Shows a provider score, or hides it entirely when the item has none.
 *
 * Absent scores are stored as -1, and every list that shows one used to format that straight to
 * "-1.0". An item TMDB doesn't know - a Floppy manual entry - never has a score, so the number and
 * its star icon are hidden together rather than rendered as a placeholder.
 *
 * [starIcon] is the icon sitting beside the value, hidden alongside it. Pass null where the layout
 * has none.
 *
 * Spoiler handling is folded in because it always accompanied this: when scores are hidden the
 * real value is stashed in the view's tag, and [isTapToReveal] lets one tap restore it.
 */
fun TextView.bindRating(
  rating: Float,
  starIcon: View? = null,
  isSpoilerHidden: Boolean = false,
  isTapToReveal: Boolean = false,
) {
  val hasRating = rating > 0F
  visibleIf(hasRating)
  starIcon?.visibleIf(hasRating)
  if (!hasRating) {
    text = ""
    return
  }

  val value = String.format(ENGLISH, "%.1f", rating)
  if (!isSpoilerHidden) {
    text = value
    return
  }

  tag = value
  text = SPOILERS_RATINGS_HIDE_SYMBOL
  if (isTapToReveal) {
    onClick { view ->
      view.tag?.let { text = it.toString() }
      view.isClickable = false
    }
  }
}
