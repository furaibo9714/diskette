package io.github.furaibo9714.diskette.ui_gallery.fanart

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.dimenToPx
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.doOnApplyWindowInsets
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.nextPage
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.openWebUrl
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.updateTopMargin
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visible
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_gallery.R
import io.github.furaibo9714.diskette.ui_gallery.databinding.FragmentArtGalleryBinding
import io.github.furaibo9714.diskette.ui_gallery.fanart.recycler.ArtGalleryAdapter
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.ImageFamily
import io.github.furaibo9714.diskette.ui_model.ImageFamily.SHOW
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.ImageType.POSTER
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_FAMILY
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_TYPE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class ArtGalleryFragment : BaseFragment<ArtGalleryViewModel>(R.layout.fragment_art_gallery) {

  override val viewModel by viewModels<ArtGalleryViewModel>()
  private val binding by viewBinding(FragmentArtGalleryBinding::bind)

  private val showId by lazy { MediaId.parse(arguments?.getString(ARG_SHOW_ID).orEmpty()) }
  private val movieId by lazy { MediaId.parse(arguments?.getString(ARG_MOVIE_ID).orEmpty()) }
  private val family by lazy { arguments?.getSerializable(ARG_FAMILY) as ImageFamily }
  private val type by lazy { arguments?.getSerializable(ARG_TYPE) as ImageType }

  private var galleryAdapter: ArtGalleryAdapter? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    if (type != POSTER) {
      requireActivity().requestedOrientation = SCREEN_ORIENTATION_FULL_USER
    }
    setupView()
    setupInsets()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          val id = if (family == SHOW) showId else movieId
          loadImages(id, family, type)
        }
      }
    }
  }

  override fun onDestroyView() {
    galleryAdapter = null
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    super.onDestroyView()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    with(binding) {
      when (newConfig.orientation) {
        ORIENTATION_LANDSCAPE -> {
          artGalleryPagerIndicatorWhite.visible()
          artGalleryPagerIndicator.gone()
          artGalleryPagerIndicatorWhite.setViewPager(artGalleryPager)
        }
        ORIENTATION_PORTRAIT -> {
          artGalleryPagerIndicatorWhite.gone()
          artGalleryPagerIndicator.visible()
          artGalleryPagerIndicator.setViewPager(artGalleryPager)
        }
        else -> {
          Timber.d("Unused orientation")
        }
      }
    }
  }

  private fun setupView() {
    with(binding) {
      artGalleryBackArrow.onClick {
        requireActivity().onBackPressed()
      }
      artGalleryBrowserIcon.onClick {
        val currentIndex = artGalleryPager.currentItem
        val image = galleryAdapter?.getItem(currentIndex)
        image?.fullFileUrl?.let { openWebUrl(it) }
      }
      galleryAdapter = ArtGalleryAdapter(
        onItemClickListener = { artGalleryPager.nextPage() },
      )
      artGalleryPager.run {
        adapter = galleryAdapter
        offscreenPageLimit = 2
        artGalleryPagerIndicator.setViewPager(this)
        adapter?.registerAdapterDataObserver(artGalleryPagerIndicator.adapterDataObserver)
      }
    }
  }

  private fun setupInsets() {
    requireView().doOnApplyWindowInsets { view, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(bottom = inset.bottom)
      with(binding) {
        artGalleryBackArrow.updateTopMargin(inset.top)
        artGalleryBrowserIcon.updateTopMargin(inset.top)
        artGalleryPagerIndicator.updateLayoutParams<MarginLayoutParams> {
          updateMargins(bottom = inset.bottom + dimenToPx(R.dimen.spaceNormal))
        }
        artGalleryPagerIndicatorWhite.updateLayoutParams<MarginLayoutParams> {
          updateMargins(bottom = inset.bottom + dimenToPx(R.dimen.spaceNormal))
        }
        artGalleryImagesProgress.updateLayoutParams<MarginLayoutParams> {
          updateMargins(bottom = inset.bottom + dimenToPx(R.dimen.spaceNormal))
        }
      }
    }
  }

  private fun render(uiState: ArtGalleryUiState) {
    uiState.run {
      with(binding) {
        images?.let {
          galleryAdapter?.setItems(it, type)
          artGalleryEmptyView.visibleIf(it.isEmpty())
          artGalleryBrowserIcon.visibleIf(it.isNotEmpty())
        }
        artGalleryImagesProgress.visibleIf(isLoading)
      }
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      isEnabled = false
      findNavControl()?.popBackStack()
    }
  }
}
