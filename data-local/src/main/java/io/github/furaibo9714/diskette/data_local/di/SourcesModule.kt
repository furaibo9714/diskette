package io.github.furaibo9714.diskette.data_local.di

import io.github.furaibo9714.diskette.data_local.database.AppDatabase
import io.github.furaibo9714.diskette.data_local.sources.ArchiveMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ArchiveShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.CustomListsItemsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.CustomListsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.DiscoverMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.DiscoverShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodeTranslationsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.EpisodesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieCollectionsItemsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieCollectionsLocalDataSource
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
import io.github.furaibo9714.diskette.data_local.sources.FloppySyncQueueLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowTranslationsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.ShowsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.TranslationsMoviesSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.TranslationsShowsSyncLogLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.WatchlistMoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.WatchlistShowsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SourcesModule {

  @Provides
  @Singleton
  internal fun providesShows(database: AppDatabase): ShowsLocalDataSource = database.showsDao()

  @Provides
  @Singleton
  internal fun providesMovies(database: AppDatabase): MoviesLocalDataSource = database.moviesDao()

  @Provides
  @Singleton
  internal fun providesArchivesShows(database: AppDatabase): ArchiveShowsLocalDataSource = database.archiveShowsDao()

  @Provides
  @Singleton
  internal fun providesArchivesMovies(database: AppDatabase): ArchiveMoviesLocalDataSource = database.archiveMoviesDao()

  @Provides
  @Singleton
  internal fun providesCustomListsItems(database: AppDatabase): CustomListsItemsLocalDataSource =
    database.customListsItemsDao()

  @Provides
  @Singleton
  internal fun providesCustomLists(database: AppDatabase): CustomListsLocalDataSource = database.customListsDao()

  @Provides
  @Singleton
  internal fun providesDiscoverShows(database: AppDatabase): DiscoverShowsLocalDataSource = database.discoverShowsDao()

  @Provides
  @Singleton
  internal fun providesDiscoverMovies(database: AppDatabase): DiscoverMoviesLocalDataSource =
    database.discoverMoviesDao()

  @Provides
  @Singleton
  internal fun providesEpisodes(database: AppDatabase): EpisodesLocalDataSource = database.episodesDao()

  @Provides
  @Singleton
  internal fun providesEpisodesSyncLog(database: AppDatabase): EpisodesSyncLogLocalDataSource =
    database.episodesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesEpisodesTranslations(database: AppDatabase): EpisodeTranslationsLocalDataSource =
    database.episodeTranslationsDao()

  @Provides
  @Singleton
  internal fun providesMovieImages(database: AppDatabase): MovieImagesLocalDataSource = database.movieImagesDao()

  @Provides
  @Singleton
  internal fun providesMovieRatings(database: AppDatabase): MovieRatingsLocalDataSource = database.movieRatingsDao()

  @Provides
  @Singleton
  internal fun providesMovieSyncLog(database: AppDatabase): MoviesSyncLogLocalDataSource = database.moviesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesMovieStreaming(database: AppDatabase): MovieStreamingsLocalDataSource =
    database.movieStreamingsDao()

  @Provides
  @Singleton
  internal fun providesMovieCollections(database: AppDatabase): MovieCollectionsLocalDataSource =
    database.movieCollectionsDao()

  @Provides
  @Singleton
  internal fun providesMovieCollectionsItems(database: AppDatabase): MovieCollectionsItemsLocalDataSource =
    database.movieCollectionsItemsDao()

  @Provides
  @Singleton
  internal fun providesMovieTranslation(database: AppDatabase): MovieTranslationsLocalDataSource =
    database.movieTranslationsDao()

  @Provides
  @Singleton
  internal fun providesMyMovies(database: AppDatabase): MyMoviesLocalDataSource = database.myMoviesDao()

  @Provides
  @Singleton
  internal fun providesMyShows(database: AppDatabase): MyShowsLocalDataSource = database.myShowsDao()

  @Provides
  @Singleton
  internal fun providesPeopleCredits(database: AppDatabase): PeopleCreditsLocalDataSource = database.peopleCreditsDao()

  @Provides
  @Singleton
  internal fun providesPeople(database: AppDatabase): PeopleLocalDataSource = database.peopleDao()

  @Provides
  @Singleton
  internal fun providesPeopleImages(database: AppDatabase): PeopleImagesLocalDataSource = database.peopleImagesDao()

  @Provides
  @Singleton
  internal fun providesPeopleShowsMovies(database: AppDatabase): PeopleShowsMoviesLocalDataSource =
    database.peopleShowsMoviesDao()

  @Provides
  @Singleton
  internal fun providesRatings(database: AppDatabase): RatingsLocalDataSource = database.ratingsDao()

  @Provides
  @Singleton
  internal fun providesRecentSearch(database: AppDatabase): RecentSearchLocalDataSource = database.recentSearchDao()

  @Provides
  @Singleton
  internal fun providesSeasons(database: AppDatabase): SeasonsLocalDataSource = database.seasonsDao()

  @Provides
  @Singleton
  internal fun providesSettings(database: AppDatabase): SettingsLocalDataSource = database.settingsDao()

  @Provides
  @Singleton
  internal fun providesShowImages(database: AppDatabase): ShowImagesLocalDataSource = database.showImagesDao()

  @Provides
  @Singleton
  internal fun providesShowRatings(database: AppDatabase): ShowRatingsLocalDataSource = database.showRatingsDao()

  @Provides
  @Singleton
  internal fun providesShowStreamings(database: AppDatabase): ShowStreamingsLocalDataSource =
    database.showStreamingsDao()

  @Provides
  @Singleton
  internal fun providesShowTranslations(database: AppDatabase): ShowTranslationsLocalDataSource =
    database.showTranslationsDao()

  @Provides
  @Singleton
  internal fun providesFloppySyncQueue(database: AppDatabase): FloppySyncQueueLocalDataSource =
    database.floppySyncQueueDao()

  @Provides
  @Singleton
  internal fun providesTranslationsMovies(database: AppDatabase): TranslationsMoviesSyncLogLocalDataSource =
    database.translationsMoviesSyncLogDao()

  @Provides
  @Singleton
  internal fun providesTranslationsShows(database: AppDatabase): TranslationsShowsSyncLogLocalDataSource =
    database.translationsSyncLogDao()

  @Provides
  @Singleton
  internal fun providesWatchlistMovies(database: AppDatabase): WatchlistMoviesLocalDataSource =
    database.watchlistMoviesDao()

  @Provides
  @Singleton
  internal fun providesWatchlistShows(database: AppDatabase): WatchlistShowsLocalDataSource =
    database.watchlistShowsDao()

  @Provides
  @Singleton
  internal fun providesRelatedMovies(database: AppDatabase): RelatedMoviesLocalDataSource = database.relatedMoviesDao()

  @Provides
  @Singleton
  internal fun providesRelatedShows(database: AppDatabase): RelatedShowsLocalDataSource = database.relatedShowsDao()
}
