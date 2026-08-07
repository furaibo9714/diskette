package io.github.furaibo9714.diskette.ui_my_movies.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.furaibo9714.diskette.ui_base.common.views.MovieView
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onLongClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_model.ImageStatus.UNAVAILABLE
import io.github.furaibo9714.diskette.ui_my_movies.common.recycler.CollectionListItem
import io.github.furaibo9714.diskette.ui_my_movies.databinding.ViewGridMovieBinding

class CollectionMovieGridView : MovieView<CollectionListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewGridMovieBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      gridMovieImage.onClick { itemClickListener?.invoke(item) }
      gridMovieImage.onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = binding.gridMovieImage
  override val placeholderView: ImageView = binding.gridMoviePlaceholder

  private lateinit var item: CollectionListItem.MovieItem

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
  ) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width * ASPECT_RATIO).toInt()
    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
  }

  override fun bind(item: CollectionListItem.MovieItem) {
    clear()
    this.item = item

    with(binding) {
      gridMovieProgress.visibleIf(item.isLoading)
      gridMovieTitle.text = if (item.translation?.title.isNullOrBlank()) item.movie.title else item.translation?.title
    }
    loadImage(item)
  }

  override fun loadImage(item: CollectionListItem.MovieItem) {
    if (item.image.status == UNAVAILABLE) {
      binding.gridMovieTitle.visible()
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: CollectionListItem.MovieItem) {
    super.onImageLoadFail(item)
    binding.gridMovieTitle.visible()
  }

  private fun clear() {
    with(binding) {
      gridMovieTitle.text = ""
      gridMovieTitle.gone()
      gridMoviePlaceholder.gone()
      gridMovieProgress.gone()
      Glide.with(this@CollectionMovieGridView).clear(gridMovieImage)
    }
  }
}
