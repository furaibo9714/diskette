package io.github.furaibo9714.diskette.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_lists.databinding.ViewListDetailsCompactItemBinding
import io.github.furaibo9714.diskette.ui_lists.details.recycler.ListDetailsItem
import java.util.Locale.ENGLISH

class ListDetailsMovieCompactItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewListDetailsCompactItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.listDetailsCompactRoot.onClick {
      if (!item.isManageMode) itemClickListener?.invoke(item)
    }
  }

  override val imageView: ImageView = binding.listDetailsCompactImage
  override val placeholderView: ImageView = binding.listDetailsCompactPlaceholder

  override fun bind(item: ListDetailsItem) {
    super.bind(item)
    clear()

    val movie = item.requireMovie()
    with(binding) {
      listDetailsCompactProgress.visibleIf(item.isLoading)
      listDetailsCompactTitle.text =
        if (item.translation?.title.isNullOrBlank()) movie.title else item.translation?.title
      listDetailsCompactRank.visibleIf(item.isRankDisplayed)
      listDetailsCompactRank.text = String.format(ENGLISH, "%d", item.rankDisplay)
    }
    loadImage(item)
  }

  private fun clear() {
    with(binding) {
      listDetailsCompactTitle.text = ""
      listDetailsCompactPlaceholder.gone()
      listDetailsCompactProgress.gone()
      listDetailsCompactRank.gone()
      Glide.with(this@ListDetailsMovieCompactItemView).clear(listDetailsCompactImage)
    }
  }
}
