package io.github.furaibo9714.diskette.ui_show.sections.ratings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.openWebUrl
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_show.R
import io.github.furaibo9714.diskette.ui_show.ShowDetailsViewModel
import io.github.furaibo9714.diskette.ui_show.databinding.FragmentShowDetailsRatingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowDetailsRatingsFragment : BaseFragment<ShowDetailsRatingsViewModel>(R.layout.fragment_show_details_ratings) {

  override val navigationId = R.id.showDetailsFragment
  private val binding by viewBinding(FragmentShowDetailsRatingsBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsRatingsViewModel>()

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    launchAndRepeatStarted(
      { parentViewModel.parentShowState.collect { it?.let { viewModel.loadRatings(it) } } },
      { parentViewModel.parentFollowedState.collect { it?.let { viewModel.refreshRatings() } } },
      { viewModel.uiState.collect { render(it) } },
    )
  }

  private fun render(uiState: ShowDetailsRatingsUiState) {
    with(uiState) {
      with(binding) {
        ratings?.let {
          if (showDetailsRatings.isBound() && !isRefreshingRatings) {
            return
          }
          showDetailsRatings.bind(ratings)
          show?.let {
            showDetailsRatings.onTmdbClick = { openLink(ShowLink.TMDB, show.ids.tmdb.id.toString()) }
          }
        }
      }
    }
  }

  private fun openLink(
    link: ShowLink,
    id: String,
  ) {
    openWebUrl(link.getUri(id)) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
  }

  override fun setupBackPressed() = Unit
}
