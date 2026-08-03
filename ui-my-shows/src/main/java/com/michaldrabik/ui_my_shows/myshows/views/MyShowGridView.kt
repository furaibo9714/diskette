package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.databinding.ViewGridShowBinding
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE

class MyShowGridView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewGridShowBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      gridShowImage.onClick { itemClickListener?.invoke(item) }
      gridShowImage.onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = binding.gridShowImage
  override val placeholderView: ImageView = binding.gridShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
  ) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width * ASPECT_RATIO).toInt()
    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
  }

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item

    with(binding) {
      gridShowProgress.visibleIf(item.isLoading)
      gridShowTitle.text = if (item.translation?.title.isNullOrBlank()) item.show.title else item.translation?.title
    }
    loadImage(item)
  }

  override fun loadImage(item: MyShowsItem) {
    if (item.image.status == UNAVAILABLE) {
      binding.gridShowTitle.visible()
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: MyShowsItem) {
    super.onImageLoadFail(item)
    binding.gridShowTitle.visible()
  }

  private fun clear() {
    with(binding) {
      gridShowTitle.text = ""
      gridShowTitle.gone()
      gridShowPlaceholder.gone()
      gridShowProgress.gone()
      Glide.with(this@MyShowGridView).clear(gridShowImage)
    }
  }
}
