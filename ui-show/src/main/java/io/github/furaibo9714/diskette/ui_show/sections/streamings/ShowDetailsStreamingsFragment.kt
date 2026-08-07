package io.github.furaibo9714.diskette.ui_show.sections.streamings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.addDivider
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_show.R
import io.github.furaibo9714.diskette.ui_show.ShowDetailsFragment
import io.github.furaibo9714.diskette.ui_show.ShowDetailsViewModel
import io.github.furaibo9714.diskette.ui_show.databinding.FragmentShowDetailsStreamingsBinding
import io.github.furaibo9714.diskette.ui_streamings.recycler.StreamingAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowDetailsStreamingsFragment :
  BaseFragment<ShowDetailsStreamingsViewModel>(
    R.layout.fragment_show_details_streamings,
  ) {

  override val navigationId = R.id.showDetailsFragment
  private val binding by viewBinding(FragmentShowDetailsStreamingsBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsStreamingsViewModel>()

  private var streamingAdapter: StreamingAdapter? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentShowState.collect { it?.let { viewModel.loadStreamings(it) } } },
      { viewModel.uiState.collect { render(it) } },
    )
  }

  private fun setupView() {
    streamingAdapter = StreamingAdapter()
    binding.showDetailsStreamingsRecycler.apply {
      setHasFixedSize(true)
      adapter = streamingAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun render(uiState: ShowDetailsStreamingsUiState) {
    with(uiState) {
      streamings?.let {
        if (streamingAdapter?.itemCount != 0) return@let
        val (items, isLocal) = it
        streamingAdapter?.setItems(items)
        if (items.isNotEmpty()) {
          (requireParentFragment() as ShowDetailsFragment).showStreamingsView(animate = !isLocal)
        }
      }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    streamingAdapter = null
    super.onDestroyView()
  }
}
