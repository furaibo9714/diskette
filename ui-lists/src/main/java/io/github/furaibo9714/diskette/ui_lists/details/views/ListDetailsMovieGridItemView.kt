package io.github.furaibo9714.diskette.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.furaibo9714.diskette.ui_base.common.views.ShowView.Companion.ASPECT_RATIO
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_lists.databinding.ViewListDetailsGridItemBinding
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import io.github.furaibo9714.diskette.ui_model.ImageStatus.UNAVAILABLE
import java.util.Locale.ENGLISH

class ListDetailsMovieGridItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewListDetailsGridItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.listDetailsGridImage.onClick {
      if (!item.isManageMode) itemClickListener?.invoke(item)
    }
  }

  override val imageView: ImageView = binding.listDetailsGridImage
  override val placeholderView: ImageView = binding.listDetailsGridPlaceholder

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
  ) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width * ASPECT_RATIO).toInt()
    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
  }

  override fun bind(item: ListDetailsItem) {
    super.bind(item)
    clear()

    val movie = item.requireMovie()
    with(binding) {
      listDetailsGridProgress.visibleIf(item.isLoading)
      listDetailsGridTitle.text = if (item.translation?.title.isNullOrBlank()) movie.title else item.translation?.title
      listDetailsGridRank.visibleIf(item.isRankDisplayed)
      listDetailsGridRank.text = String.format(ENGLISH, "%d", item.rankDisplay)
    }
    loadImage(item)
  }

  override fun loadImage(item: ListDetailsItem) {
    if (item.image.status == UNAVAILABLE) {
      binding.listDetailsGridTitle.visible()
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: ListDetailsItem) {
    super.onImageLoadFail(item)
    binding.listDetailsGridTitle.visible()
  }

  private fun clear() {
    with(binding) {
      listDetailsGridTitle.text = ""
      listDetailsGridTitle.gone()
      listDetailsGridPlaceholder.gone()
      listDetailsGridProgress.gone()
      listDetailsGridRank.gone()
      Glide.with(this@ListDetailsMovieGridItemView).clear(listDetailsGridImage)
    }
  }
}
