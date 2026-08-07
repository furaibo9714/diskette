package io.github.furaibo9714.diskette.ui_people.gallery.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.dimenToPx
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_people.R
import io.github.furaibo9714.diskette.ui_people.databinding.ViewPersonGalleryImageBinding

class PersonGalleryImageView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewPersonGalleryImageBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.galleryImageCorner) }
  var onItemClickListener: (() -> Unit)? = null

  fun bind(image: Image) {
    clear()
    binding.viewPersonGalleryImage.onClick { onItemClickListener?.invoke() }
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    Glide
      .with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .into(binding.viewPersonGalleryImage)
  }

  private fun clear() {
    Glide.with(this).clear(binding.viewPersonGalleryImage)
  }
}
