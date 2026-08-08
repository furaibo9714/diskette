package io.github.furaibo9714.diskette.ui_my_shows.myshows.views

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
import io.github.furaibo9714.diskette.ui_base.common.views.ShowView
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.bindRating
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.dimenToPx
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onLongClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.setOutboundRipple
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_my_shows.R
import io.github.furaibo9714.diskette.ui_my_shows.databinding.ViewCollectionShowBinding
import io.github.furaibo9714.diskette.ui_my_shows.myshows.recycler.MyShowsItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyShowAllView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    clipChildren = false
    clipToPadding = false

    with(binding) {
      collectionShowRoot.onClick { itemClickListener?.invoke(item) }
      collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
      collectionShowRoot.setOutboundRipple(
        size = (context.dimenToPx(R.dimen.collectionItemRippleSpace)).toFloat(),
        corner = context.dimenToPx(R.dimen.mediaTileCorner).toFloat(),
      )
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item

    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)
      collectionShowTitle.text =
        if (item.translation?.title.isNullOrBlank()) {
          item.show.title
        } else {
          item.translation?.title
        }

      bindDescription(item)
      bindRating(item)

      collectionShowNetwork.text =
        if (item.show.year > 0) {
          context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
        } else {
          String.format("%s", item.show.network)
        }

      collectionShowNetwork.visibleIf(item.show.network.isNotBlank())

      item.userRating?.let {
        collectionShowUserStarIcon.visible()
        collectionShowUserRating.visible()
        collectionShowUserRating.text = String.format(ENGLISH, "%d", it)
      }
    }
    loadImage(item)
  }

  private fun bindDescription(item: MyShowsItem) {
    with(binding) {
      var description =
        if (item.translation?.overview.isNullOrBlank()) {
          item.show.overview
        } else {
          item.translation?.overview
        }

      if (item.spoilers.isSpoilerHidden) {
        collectionShowDescription.tag = description.toString()
        description = SPOILERS_REGEX.replace(description.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isSpoilerTapToReveal) {
          collectionShowDescription.onClick { view ->
            view.tag?.let { collectionShowDescription.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      collectionShowDescription.text = description
      collectionShowDescription.visibleIf(item.show.overview.isNotBlank())
    }
  }

  private fun bindRating(item: MyShowsItem) {
    binding.collectionShowRating.bindRating(
      rating = item.show.rating,
      starIcon = binding.collectionShowStarIcon,
      isSpoilerHidden = item.spoilers.isSpoilerRatingsHidden,
      isTapToReveal = item.spoilers.isSpoilerTapToReveal,
    )
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionShowTitle.text = ""
      collectionShowDescription.text = ""
      collectionShowNetwork.text = ""
      collectionShowRating.text = ""
      collectionShowPlaceholder.gone()
      collectionShowUserStarIcon.gone()
      collectionShowUserRating.gone()
      Glide.with(this@MyShowAllView).clear(collectionShowImage)
    }
  }
}
