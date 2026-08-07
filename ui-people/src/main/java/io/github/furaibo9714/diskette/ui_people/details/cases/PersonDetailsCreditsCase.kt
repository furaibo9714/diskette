package io.github.furaibo9714.diskette.ui_people.details.cases

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.PeopleRepository
import io.github.furaibo9714.diskette.repository.TranslationsRepository
import io.github.furaibo9714.diskette.repository.images.MovieImagesProvider
import io.github.furaibo9714.diskette.repository.images.ShowImagesProvider
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.repository.shows.ShowsRepository
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.Person
import io.github.furaibo9714.diskette.ui_model.PersonCredit
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.SpoilersSettings
import io.github.furaibo9714.diskette.ui_people.details.filters.PersonDetailsFilters
import io.github.furaibo9714.diskette.ui_people.details.recycler.PersonDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsCreditsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val peopleRepository: PeopleRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  suspend fun loadCredits(
    person: Person,
    filters: PersonDetailsFilters,
  ) = withContext(dispatchers.IO) {
    val (modes, onlyCollection) = filters

    val myShowsIdsAsync = async { showsRepository.myShows.loadAllIds() }
    val myMoviesIdsAsync = async { moviesRepository.myMovies.loadAllIds() }
    val watchlistShowsIdsAsync = async { showsRepository.watchlistShows.loadAllIds() }
    val watchlistMoviesIdsAsync = async { moviesRepository.watchlistMovies.loadAllIds() }
    val spoilers = settingsRepository.spoilers.getAll()

    val (myShowsIds, myMoviesIds, watchlistShowsId, watchlistMoviesIds) = awaitAll(
      myShowsIdsAsync,
      myMoviesIdsAsync,
      watchlistShowsIdsAsync,
      watchlistMoviesIdsAsync,
    )

    val showsCollectionIds = (myShowsIds + watchlistShowsId).toSet()
    val moviesCollectionIds = (myMoviesIds + watchlistMoviesIds).toSet()

    val credits = peopleRepository.loadCredits(person)
    credits
      .filter {
        val filterByRelease = (it.releaseDate != null || (it.releaseDate == null && it.isUpcoming))

        val filterByCollection = if (onlyCollection) {
          it.show?.traktId in showsCollectionIds || it.movie?.traktId in moviesCollectionIds
        } else {
          true
        }

        val filterByMode = when {
          modes.isEmpty() || modes.containsAll(Mode.entries) -> true
          modes.contains(Mode.SHOWS) -> it.show != null
          modes.contains(Mode.MOVIES) -> it.movie != null
          else -> true
        }

        filterByMode && filterByRelease && filterByCollection
      }.sortedWith(
        compareByDescending<PersonCredit> { it.releaseDate == null }.thenByDescending { it.releaseDate?.toEpochDay() },
      ).map {
        async {
          when {
            it.show != null -> createShowItem(
              show = it.requireShow(),
              myShowsIds = myShowsIds,
              watchlistShowsId = watchlistShowsId,
              spoilersSettings = spoilers,
            )
            it.movie != null -> createMovieItem(
              movie = it.requireMovie(),
              myMoviesIds = myMoviesIds,
              watchlistMoviesId = watchlistMoviesIds,
              spoilersSettings = spoilers,
            )
            else -> throw IllegalStateException()
          }
        }
      }.awaitAll()
      .groupBy {
        it.getReleaseDate()?.year
      }
  }

  private suspend fun createShowItem(
    show: Show,
    myShowsIds: List<Long>,
    watchlistShowsId: List<Long>,
    spoilersSettings: SpoilersSettings,
  ) = show.let {
    val isMyShow = it.traktId in myShowsIds
    val isWatchlist = it.traktId in watchlistShowsId
    val image = showImagesProvider.findCachedImage(it, ImageType.POSTER)
    val translation = when (val language = translationsRepository.getLanguage()) {
      Config.DEFAULT_LANGUAGE -> null
      else -> translationsRepository.loadTranslation(it, language, onlyLocal = true)
    }
    PersonDetailsItem.CreditsShowItem(
      show = it,
      image = image,
      isMy = isMyShow,
      isWatchlist = isWatchlist,
      translation = translation,
      spoilers = spoilersSettings,
    )
  }

  private suspend fun createMovieItem(
    movie: Movie,
    myMoviesIds: List<Long>,
    watchlistMoviesId: List<Long>,
    spoilersSettings: SpoilersSettings,
  ) = movie.let {
    val isWatched = it.traktId in myMoviesIds
    val isWatchlist = it.traktId in watchlistMoviesId
    val image = movieImagesProvider.findCachedImage(it, ImageType.POSTER)
    val translation = when (val language = translationsRepository.getLanguage()) {
      Config.DEFAULT_LANGUAGE -> null
      else -> translationsRepository.loadTranslation(it, language, onlyLocal = true)
    }
    PersonDetailsItem.CreditsMovieItem(
      movie = it,
      image = image,
      isMy = isWatched,
      isWatchlist = isWatchlist,
      translation = translation,
      spoilers = spoilersSettings,
      moviesEnabled = settingsRepository.isMoviesEnabled,
    )
  }
}
