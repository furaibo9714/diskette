package io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.ui_base.BaseBottomSheetFragment
import io.github.furaibo9714.diskette.ui_base.R
import io.github.furaibo9714.diskette.ui_base.databinding.ViewContextMenuBinding
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.dimenToPx
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.requireParcelable
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showErrorSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showInfoSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.withFailListener
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.withSuccessListener
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageStatus
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU

abstract class ContextMenuBottomSheet : BaseBottomSheetFragment(R.layout.view_context_menu) {

  companion object {
    private const val ARG_SHOW_PIN_BUTTONS = "ARG_SHOW_PIN_BUTTONS"
    private const val ARG_DETAILS_ENABLED = "ARG_DETAILS_ENABLED"

    fun createBundle(
      mediaId: MediaId,
      showPinButtons: Boolean = false,
      detailsEnabled: Boolean = true,
    ) = bundleOf(
      ARG_ID to mediaId,
      ARG_OPTIONS to bundleOf(
        ARG_SHOW_PIN_BUTTONS to showPinButtons,
        ARG_DETAILS_ENABLED to detailsEnabled,
      ),
    )
  }

  protected val binding by viewBinding(ViewContextMenuBinding::bind)

  protected val itemId by lazy { requireParcelable<MediaId>(ARG_ID) }
  private val showPinButtons by lazy { requireParcelable<Bundle>(ARG_OPTIONS).getBoolean(ARG_SHOW_PIN_BUTTONS) }
  private val detailsEnabled by lazy { requireParcelable<Bundle>(ARG_OPTIONS).getBoolean(ARG_DETAILS_ENABLED) }

  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val cornerBigRadius by lazy { dimenToPx(R.dimen.collectionItemCorner).toFloat() }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy {
    GranularRoundedCorners(cornerBigRadius, cornerRadius, cornerRadius, cornerRadius)
  }

  protected val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
  protected val colorGray by lazy { ContextCompat.getColor(requireContext(), R.color.colorGrayLight) }

  protected abstract fun openDetails()

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  protected open fun setupView() {
    with(binding) {
      contextMenuItemDescription.setInitialLines(5)
      contextMenuItemPinButtonsLayout.visibleIf(showPinButtons)
      contextMenuItemSeparator2.visibleIf(showPinButtons)
      contextMenuItemImage.onClick { if (detailsEnabled) openDetails() }
      contextMenuItemPlaceholder.onClick { if (detailsEnabled) openDetails() }
    }
  }

  protected fun renderImage(image: Image) {
    Glide.with(this).clear(binding.contextMenuItemImage)

    if (image.status == ImageStatus.UNAVAILABLE) {
      binding.contextMenuItemPlaceholder.visible()
      binding.contextMenuItemImage.gone()
      return
    }

    Glide
      .with(this)
      .load(image.fullFileUrl)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        binding.contextMenuItemPlaceholder.gone()
        binding.contextMenuItemImage.visible()
      }.withFailListener {
        binding.contextMenuItemPlaceholder.visible()
        binding.contextMenuItemImage.gone()
      }.into(binding.contextMenuItemImage)
  }

  protected fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.contextMenuItemSnackbarHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.contextMenuItemSnackbarHost.showErrorSnackbar(getString(message.textRestId))
    }
  }

  protected fun close() {
    setFragmentResult(REQUEST_ITEM_MENU, Bundle.EMPTY)
    closeSheet()
    dismiss()
  }
}
