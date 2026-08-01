package com.michaldrabik.ui_movie.sections.ratings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.FragmentMovieDetailsRatingsBinding
import com.michaldrabik.ui_movie.helpers.MovieLink
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsRatingsFragment :
  BaseFragment<MovieDetailsRatingsViewModel>(
    R.layout.fragment_movie_details_ratings,
  ) {

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsRatingsViewModel>()
  private val binding by viewBinding(FragmentMovieDetailsRatingsBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    launchAndRepeatStarted(
      { parentViewModel.parentMovieState.collect { it?.let { viewModel.loadRatings(it) } } },
      { parentViewModel.parentFollowedState.collect { it?.let { viewModel.refreshRatings() } } },
      { viewModel.uiState.collect { render(it) } },
    )
  }

  private fun render(uiState: MovieDetailsRatingsUiState) {
    with(uiState) {
      with(binding) {
        ratings?.let {
          if (movieDetailsRatings.isBound() && !isRefreshingRatings) {
            return
          }
          movieDetailsRatings.bind(ratings)
          movie?.let {
            movieDetailsRatings.onTmdbClick = { openMovieLink(MovieLink.TMDB, movie.ids.tmdb.id.toString()) }
          }
        }
      }
    }
  }

  private fun openMovieLink(
    link: MovieLink,
    id: String,
  ) {
    openWebUrl(link.getUri(id)) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
  }

  override fun setupBackPressed() = Unit
}
