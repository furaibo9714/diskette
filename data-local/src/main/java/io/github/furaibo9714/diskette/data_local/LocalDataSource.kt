package io.github.furaibo9714.diskette.data_local

import io.github.furaibo9714.diskette.data_local.sources.ArchiveMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ArchiveShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.CustomListsItemsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.CustomListsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.DiscoverMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.DiscoverShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodeTranslationsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.FloppySyncQueueLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieImagesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieRatingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieStreamingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieTranslationsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MoviesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MyMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MyShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.PeopleCreditsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.PeopleImagesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.PeopleLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.PeopleShowsMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.RatingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.RecentSearchLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.RelatedMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.RelatedShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.SeasonsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.SettingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowImagesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowRatingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowStreamingsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowTranslationsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.TranslationsMoviesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.TranslationsShowsSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.WatchlistMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.WatchlistShowsLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

// TODO Refactor. Split or remove this wrapper at all. Clients do not need to be exposed to everything.

/**
 * Provides local data sources access points.
 */
interface LocalDataSource {
  val archiveMovies: ArchiveMoviesLocalDataSource
  val archiveShows: ArchiveShowsLocalDataSource
  val customLists: CustomListsLocalDataSource
  val customListsItems: CustomListsItemsLocalDataSource
  val discoverMovies: DiscoverMoviesLocalDataSource
  val discoverShows: DiscoverShowsLocalDataSource
  val episodes: EpisodesLocalDataSource
  val episodesSyncLog: EpisodesSyncLogLocalDataSource
  val episodesTranslations: EpisodeTranslationsLocalDataSource
  val floppySyncQueue: FloppySyncQueueLocalDataSource
  val movieImages: MovieImagesLocalDataSource
  val movieRatings: MovieRatingsLocalDataSource
  val movieStreamings: MovieStreamingsLocalDataSource
  val movieTranslations: MovieTranslationsLocalDataSource
  val movies: MoviesLocalDataSource
  val moviesSyncLog: MoviesSyncLogLocalDataSource
  val myMovies: MyMoviesLocalDataSource
  val myShows: MyShowsLocalDataSource
  val people: PeopleLocalDataSource
  val peopleCredits: PeopleCreditsLocalDataSource
  val peopleImages: PeopleImagesLocalDataSource
  val peopleShowsMovies: PeopleShowsMoviesLocalDataSource
  val ratings: RatingsLocalDataSource
  val recentSearch: RecentSearchLocalDataSource
  val relatedMovies: RelatedMoviesLocalDataSource
  val relatedShows: RelatedShowsLocalDataSource
  val seasons: SeasonsLocalDataSource
  val settings: SettingsLocalDataSource
  val showImages: ShowImagesLocalDataSource
  val showRatings: ShowRatingsLocalDataSource
  val showStreamings: ShowStreamingsLocalDataSource
  val showTranslations: ShowTranslationsLocalDataSource
  val shows: ShowsLocalDataSource
  val translationsMoviesSyncLog: TranslationsMoviesSyncLogLocalDataSource
  val translationsShowsSyncLog: TranslationsShowsSyncLogLocalDataSource
  val watchlistMovies: WatchlistMoviesLocalDataSource
  val watchlistShows: WatchlistShowsLocalDataSource
}

@Singleton
internal class MainLocalDataSource @Inject constructor(
  override val archiveMovies: ArchiveMoviesLocalDataSource,
  override val archiveShows: ArchiveShowsLocalDataSource,
  override val customLists: CustomListsLocalDataSource,
  override val customListsItems: CustomListsItemsLocalDataSource,
  override val discoverMovies: DiscoverMoviesLocalDataSource,
  override val discoverShows: DiscoverShowsLocalDataSource,
  override val episodes: EpisodesLocalDataSource,
  override val episodesSyncLog: EpisodesSyncLogLocalDataSource,
  override val episodesTranslations: EpisodeTranslationsLocalDataSource,
  override val floppySyncQueue: FloppySyncQueueLocalDataSource,
  override val movieImages: MovieImagesLocalDataSource,
  override val movieRatings: MovieRatingsLocalDataSource,
  override val movieStreamings: MovieStreamingsLocalDataSource,
  override val movieTranslations: MovieTranslationsLocalDataSource,
  override val movies: MoviesLocalDataSource,
  override val moviesSyncLog: MoviesSyncLogLocalDataSource,
  override val myMovies: MyMoviesLocalDataSource,
  override val myShows: MyShowsLocalDataSource,
  override val people: PeopleLocalDataSource,
  override val peopleCredits: PeopleCreditsLocalDataSource,
  override val peopleImages: PeopleImagesLocalDataSource,
  override val peopleShowsMovies: PeopleShowsMoviesLocalDataSource,
  override val ratings: RatingsLocalDataSource,
  override val recentSearch: RecentSearchLocalDataSource,
  override val relatedMovies: RelatedMoviesLocalDataSource,
  override val relatedShows: RelatedShowsLocalDataSource,
  override val seasons: SeasonsLocalDataSource,
  override val settings: SettingsLocalDataSource,
  override val showImages: ShowImagesLocalDataSource,
  override val showRatings: ShowRatingsLocalDataSource,
  override val showStreamings: ShowStreamingsLocalDataSource,
  override val showTranslations: ShowTranslationsLocalDataSource,
  override val shows: ShowsLocalDataSource,
  override val translationsMoviesSyncLog: TranslationsMoviesSyncLogLocalDataSource,
  override val translationsShowsSyncLog: TranslationsShowsSyncLogLocalDataSource,
  override val watchlistMovies: WatchlistMoviesLocalDataSource,
  override val watchlistShows: WatchlistShowsLocalDataSource,
) : LocalDataSource
