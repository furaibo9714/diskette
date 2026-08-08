package io.github.furaibo9714.diskette.repository.shows.ratings

import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.Ratings
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsExternalRatingsRepository @Inject constructor(
  private val mappers: Mappers,
) {

  /**
   * TMDB's score already arrives with the show's own details, so it is read straight off the
   * loaded show. Fetching it again from Floppy would cost a round-trip for the same number and
   * would leave the rating blank whenever no Floppy server is configured.
   */
  fun loadRatings(show: Show): Ratings = mappers.ratings.fromShow(show)
}
