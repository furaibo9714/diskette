package io.github.furaibo9714.diskette.ui_movie.sections.ratings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.openWebUrl
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_movie.MovieDetailsViewModel
import io.github.furaibo9714.diskette.ui_movie.R
import io.github.furaibo9714.diskette.ui_movie.databinding.FragmentMovieDetailsRatingsBinding
import io.github.furaibo9714.diskette.ui_movie.helpers.MovieLink
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
        /**
         * The strip offers a TMDB link when no score is cached yet. An item TMDB doesn't know - a
         * Floppy manual entry - has neither, so the whole section is hidden rather than showing a
         * link that leads nowhere.
         */
        val isOnTmdb = movie?.ids?.media?.tmdbIdOrNull != null
        movieDetailsRatings.visibleIf(isOnTmdb)
        if (!isOnTmdb) return

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
