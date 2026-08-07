package io.github.furaibo9714.diskette.ui_my_movies.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.furaibo9714.diskette.ui_base.common.views.MovieView
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.dimenToPx
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onLongClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.setOutboundRipple
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_my_movies.R
import io.github.furaibo9714.diskette.ui_my_movies.common.recycler.CollectionListItem
import io.github.furaibo9714.diskette.ui_my_movies.databinding.ViewCompactMovieBinding

class CollectionMovieCompactView : MovieView<CollectionListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCompactMovieBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    clipChildren = false
    clipToPadding = false

    with(binding.compactMovieRoot) {
      onClick { itemClickListener?.invoke(item) }
      onLongClick { itemLongClickListener?.invoke(item) }
      setOutboundRipple(
        size = (context.dimenToPx(R.dimen.collectionItemRippleSpace)).toFloat(),
        corner = context.dimenToPx(R.dimen.mediaTileCorner).toFloat(),
      )
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.compactMovieImage
  override val placeholderView: ImageView = binding.compactMoviePlaceholder

  private lateinit var item: CollectionListItem.MovieItem

  override fun bind(item: CollectionListItem.MovieItem) {
    clear()
    this.item = item

    with(binding) {
      compactMovieProgress.visibleIf(item.isLoading)
      compactMovieTitle.text =
        if (item.translation?.title.isNullOrBlank()) {
          item.movie.title
        } else {
          item.translation?.title
        }
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      compactMovieTitle.text = ""
      compactMoviePlaceholder.gone()
      compactMovieProgress.gone()
      Glide.with(this@CollectionMovieCompactView).clear(compactMovieImage)
    }
  }
}
