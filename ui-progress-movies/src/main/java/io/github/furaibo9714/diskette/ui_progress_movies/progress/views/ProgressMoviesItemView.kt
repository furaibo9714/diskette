package io.github.furaibo9714.diskette.ui_progress_movies.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.furaibo9714.diskette.common.Config.SPOILERS_HIDE_SYMBOL
import io.github.furaibo9714.diskette.common.Config.SPOILERS_REGEX
import io.github.furaibo9714.diskette.ui_base.common.views.MovieView
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.bindRating
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.addRipple
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.bump
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.colorStateListFromAttr
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.expandTouch
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onLongClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_model.SortOrder.RATING
import io.github.furaibo9714.diskette.ui_model.SortOrder.RUNTIME
import io.github.furaibo9714.diskette.ui_model.SortOrder.USER_RATING
import io.github.furaibo9714.diskette.ui_progress_movies.R
import io.github.furaibo9714.diskette.ui_progress_movies.databinding.ViewProgressMoviesMainItemBinding
import io.github.furaibo9714.diskette.ui_progress_movies.progress.recycler.ProgressMovieListItem
import java.util.Locale

@SuppressLint("SetTextI18n")
class ProgressMoviesItemView : MovieView<ProgressMovieListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressMoviesMainItemBinding.inflate(LayoutInflater.from(context), this)

  var checkClickListener: ((ProgressMovieListItem.MovieItem) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    binding.progressMovieItemCheckButton.expandTouch(100)
    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: ProgressMovieListItem.MovieItem

  override val imageView: ImageView = binding.progressMovieItemImage
  override val placeholderView: ImageView = binding.progressMovieItemPlaceholder

  override fun bind(item: ProgressMovieListItem.MovieItem) {
    this.item = item
    clear()

    with(binding) {
      val translationTitle = item.translation?.title
      progressMovieItemTitle.text =
        if (translationTitle.isNullOrBlank()) {
          item.movie.title
        } else {
          translationTitle
        }

      bindDescription(item)
      bindRating(item)
      bindRuntime(item)

      progressMovieItemPin.visibleIf(item.isPinned)
      progressMovieItemCheckButton.onClick {
        it.bump { checkClickListener?.invoke(item) }
      }

      loadImage(item)
    }
  }

  private fun bindDescription(item: ProgressMovieListItem.MovieItem) {
    var description = if (item.translation?.overview.isNullOrBlank()) {
      item.movie.overview.ifBlank { context.getString(R.string.textNoDescription) }
    } else {
      item.translation?.overview
    }

    with(binding) {
      if (item.spoilers.isWatchlistMoviesHidden) {
        progressMovieItemSubtitle.tag = description
        description = SPOILERS_REGEX.replace(description.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isTapToReveal) {
          progressMovieItemSubtitle.onClick { view ->
            view.tag?.let { progressMovieItemSubtitle.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      progressMovieItemSubtitle.text = description
    }
  }

  private fun bindRating(item: ProgressMovieListItem.MovieItem) {
    with(binding) {
      when (item.sortOrder) {
        RATING -> {
          progressMovieItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.colorAccent)
          progressMovieItemRating.bindRating(
            rating = item.movie.rating,
            starIcon = progressMovieItemRatingStar,
            isSpoilerHidden = item.spoilers.isMyShowsRatingsHidden,
            isTapToReveal = item.spoilers.isTapToReveal,
          )
        }
        USER_RATING -> {
          val hasRating = item.userRating != null
          progressMovieItemRating.visibleIf(hasRating)
          progressMovieItemRatingStar.visibleIf(hasRating)
          progressMovieItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.textColorPrimary)
          progressMovieItemRating.text = String.format(Locale.ENGLISH, "%d", item.userRating)
        }
        else -> {
          progressMovieItemRating.gone()
          progressMovieItemRatingStar.gone()
        }
      }
    }
  }

  private fun bindRuntime(item: ProgressMovieListItem.MovieItem) {
    with(binding) {
      progressMovieItemRuntime.gone()
      progressMovieItemRuntimeIcon.gone()

      if (item.movie.runtime <= 0 || item.sortOrder != RUNTIME) {
        return
      }

      progressMovieItemRuntimeIcon.visible()
      progressMovieItemRuntime.visible()
      progressMovieItemRuntime.text = "${item.movie.runtime} ${context.getString(R.string.textMinutesShort)}"
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      progressMovieItemTitle.text = ""
      progressMovieItemSubtitle.text = ""
      progressMovieItemPlaceholder.gone()
      Glide.with(this@ProgressMoviesItemView).clear(progressMovieItemImage)
    }
  }
}
