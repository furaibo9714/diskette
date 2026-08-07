package io.github.furaibo9714.diskette.ui_movie.sections.collections.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.furaibo9714.diskette.ui_base.BaseBottomSheetFragment
import io.github.furaibo9714.diskette.ui_base.common.FastLinearLayoutManager
import io.github.furaibo9714.diskette.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.fadeIn
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.fadeOut
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.requireParcelable
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.screenHeight
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showErrorSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showInfoSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_movie.R
import io.github.furaibo9714.diskette.ui_movie.databinding.ViewMovieCollectionDetailsBinding
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionAdapter
import io.github.furaibo9714.diskette.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_COLLECTION_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import io.github.furaibo9714.diskette.ui_navigation.java.NavigationArgs.REQUEST_DETAILS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsCollectionBottomSheet : BaseBottomSheetFragment(R.layout.view_movie_collection_details) {

  companion object {
    const val SHOW_BACK_UP_BUTTON_THRESHOLD = 25

    fun createBundle(
      collectionId: IdTrakt,
      sourceMovieId: IdTrakt,
    ) = bundleOf(
      ARG_ID to collectionId,
      ARG_MOVIE_ID to sourceMovieId,
    )
  }

  private val viewModel by viewModels<MovieDetailsCollectionViewModel>()
  private val binding by viewBinding(ViewMovieCollectionDetailsBinding::bind)

  private val collectionId by lazy { requireParcelable<IdTrakt>(ARG_ID) }
  private val sourceMovieId by lazy { requireParcelable<IdTrakt>(ARG_MOVIE_ID) }

  private var adapter: MovieDetailsCollectionAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    setupView()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      doAfterLaunch = {
        viewModel.loadCollection(collectionId)
      },
    )
  }

  private fun setupView() {
    with(binding) {
      val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
      with(behavior) {
        peekHeight = (screenHeight() * 0.55).toInt()
        skipCollapsed = true
        state = BottomSheetBehavior.STATE_COLLAPSED
      }
      backToTopButton.onClick {
        backToTopButton.fadeOut(150)
        itemsRecycler.smoothScrollToPosition(0)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = FastLinearLayoutManager(context, VERTICAL, false)
    adapter = MovieDetailsCollectionAdapter(
      onItemClickListener = ::openDetails,
      onItemLongClickListener = ::openContextDetails,
      onMissingImageListener = viewModel::loadMissingImage,
      onMissingTranslationListener = viewModel::loadMissingTranslation,
    )
    with(binding.itemsRecycler) {
      adapter = this@MovieDetailsCollectionBottomSheet.adapter
      layoutManager = this@MovieDetailsCollectionBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      removeOnScrollListener(recyclerScrollListener)
      addOnScrollListener(recyclerScrollListener)
    }
  }

  private fun openDetails(item: MovieDetailsCollectionItem) {
    if (item !is MovieDetailsCollectionItem.MovieItem) return
    if (item.movie.ids.trakt == sourceMovieId) {
      dismiss()
      return
    }

    val resultBundle = bundleOf(ARG_COLLECTION_ID to collectionId)
    setFragmentResult(REQUEST_DETAILS, resultBundle)

    val argsBundle = bundleOf(ARG_MOVIE_ID to item.movie.traktId)
    requireParentFragment()
      .findNavController()
      .navigate(R.id.actionMovieCollectionDialogToMovie, argsBundle)
  }

  private fun openContextDetails(item: MovieDetailsCollectionItem) {
    if (item !is MovieDetailsCollectionItem.MovieItem) return
    if (item.movie.ids.trakt == sourceMovieId) return

    setFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == NavigationArgs.REQUEST_ITEM_MENU) {
        viewModel.loadCollection(collectionId)
      }
      clearFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU)
    }

    val bundle = ContextMenuBottomSheet.createBundle(
      idTrakt = item.movie.ids.trakt,
      detailsEnabled = false,
    )
    navigateTo(R.id.actionMovieCollectionDialogToContextDialog, bundle)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: MovieDetailsCollectionUiState) {
    uiState.run {
      items?.let { adapter?.setItems(it) }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.rootLayout.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.rootLayout.showErrorSnackbar(getString(message.textRestId))
    }
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(
      recyclerView: RecyclerView,
      newState: Int,
    ) {
      if (newState != SCROLL_STATE_IDLE) {
        return
      }
      if ((layoutManager?.findFirstVisibleItemPosition() ?: 0) >= SHOW_BACK_UP_BUTTON_THRESHOLD) {
        binding.backToTopButton.fadeIn(150)
      } else {
        binding.backToTopButton.fadeOut(150)
      }
    }
  }
}
