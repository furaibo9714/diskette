package io.github.furaibo9714.diskette.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.furaibo9714.diskette.data_local.database.dao.ArchiveMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.ArchiveShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.CustomListsDao
import io.github.furaibo9714.diskette.data_local.database.dao.CustomListsItemsDao
import io.github.furaibo9714.diskette.data_local.database.dao.DiscoverMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.DiscoverShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.EpisodeTranslationsDao
import io.github.furaibo9714.diskette.data_local.database.dao.EpisodesDao
import io.github.furaibo9714.diskette.data_local.database.dao.EpisodesSyncLogDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieCollectionsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieCollectionsItemsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieImagesDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieRatingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieStreamingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MovieTranslationsDao
import io.github.furaibo9714.diskette.data_local.database.dao.MoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.MoviesSyncLogDao
import io.github.furaibo9714.diskette.data_local.database.dao.MyMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.MyShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.PeopleCreditsDao
import io.github.furaibo9714.diskette.data_local.database.dao.PeopleDao
import io.github.furaibo9714.diskette.data_local.database.dao.PeopleImagesDao
import io.github.furaibo9714.diskette.data_local.database.dao.PeopleShowsMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.RatingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.RecentSearchDao
import io.github.furaibo9714.diskette.data_local.database.dao.RelatedMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.RelatedShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.SeasonsDao
import io.github.furaibo9714.diskette.data_local.database.dao.SettingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowImagesDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowRatingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowStreamingsDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowTranslationsDao
import io.github.furaibo9714.diskette.data_local.database.dao.ShowsDao
import io.github.furaibo9714.diskette.data_local.database.dao.FloppySyncQueueDao
import io.github.furaibo9714.diskette.data_local.database.dao.TranslationsMoviesSyncLogDao
import io.github.furaibo9714.diskette.data_local.database.dao.TranslationsSyncLogDao
import io.github.furaibo9714.diskette.data_local.database.dao.WatchlistMoviesDao
import io.github.furaibo9714.diskette.data_local.database.dao.WatchlistShowsDao
import io.github.furaibo9714.diskette.data_local.database.migrations.DATABASE_VERSION
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveMovie
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveShow
import io.github.furaibo9714.diskette.data_local.database.model.CustomList
import io.github.furaibo9714.diskette.data_local.database.model.CustomListItem
import io.github.furaibo9714.diskette.data_local.database.model.DiscoverMovie
import io.github.furaibo9714.diskette.data_local.database.model.DiscoverShow
import io.github.furaibo9714.diskette.data_local.database.model.Episode
import io.github.furaibo9714.diskette.data_local.database.model.EpisodeTranslation
import io.github.furaibo9714.diskette.data_local.database.model.EpisodesSyncLog
import io.github.furaibo9714.diskette.data_local.database.model.Movie
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollection
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollectionItem
import io.github.furaibo9714.diskette.data_local.database.model.MovieImage
import io.github.furaibo9714.diskette.data_local.database.model.MovieRatings
import io.github.furaibo9714.diskette.data_local.database.model.MovieStreaming
import io.github.furaibo9714.diskette.data_local.database.model.MovieTranslation
import io.github.furaibo9714.diskette.data_local.database.model.MoviesSyncLog
import io.github.furaibo9714.diskette.data_local.database.model.MyMovie
import io.github.furaibo9714.diskette.data_local.database.model.MyShow
import io.github.furaibo9714.diskette.data_local.database.model.Person
import io.github.furaibo9714.diskette.data_local.database.model.PersonCredits
import io.github.furaibo9714.diskette.data_local.database.model.PersonImage
import io.github.furaibo9714.diskette.data_local.database.model.PersonShowMovie
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.data_local.database.model.RecentSearch
import io.github.furaibo9714.diskette.data_local.database.model.RelatedMovie
import io.github.furaibo9714.diskette.data_local.database.model.RelatedShow
import io.github.furaibo9714.diskette.data_local.database.model.Season
import io.github.furaibo9714.diskette.data_local.database.model.Settings
import io.github.furaibo9714.diskette.data_local.database.model.Show
import io.github.furaibo9714.diskette.data_local.database.model.ShowImage
import io.github.furaibo9714.diskette.data_local.database.model.ShowRatings
import io.github.furaibo9714.diskette.data_local.database.model.ShowStreaming
import io.github.furaibo9714.diskette.data_local.database.model.ShowTranslation
import io.github.furaibo9714.diskette.data_local.database.model.TranslationsMoviesSyncLog
import io.github.furaibo9714.diskette.data_local.database.model.TranslationsSyncLog
import io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistMovie
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistShow

@Database(
  version = DATABASE_VERSION,
  entities = [
    Show::class,
    Movie::class,
    DiscoverShow::class,
    DiscoverMovie::class,
    MyShow::class,
    MyMovie::class,
    WatchlistShow::class,
    WatchlistMovie::class,
    ArchiveShow::class,
    ArchiveMovie::class,
    RelatedShow::class,
    RelatedMovie::class,
    ShowImage::class,
    MovieImage::class,
    Season::class,
    Person::class,
    PersonShowMovie::class,
    PersonCredits::class,
    PersonImage::class,
    Episode::class,
    Settings::class,
    RecentSearch::class,
    EpisodesSyncLog::class,
    MoviesSyncLog::class,
    TranslationsSyncLog::class,
    TranslationsMoviesSyncLog::class,
    ShowTranslation::class,
    MovieTranslation::class,
    EpisodeTranslation::class,
    CustomList::class,
    CustomListItem::class,
    Rating::class,
    ShowRatings::class,
    MovieRatings::class,
    ShowStreaming::class,
    MovieStreaming::class,
    MovieCollection::class,
    MovieCollectionItem::class,
    FloppySyncQueue::class,
  ],
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun showsDao(): ShowsDao

  abstract fun moviesDao(): MoviesDao

  abstract fun discoverShowsDao(): DiscoverShowsDao

  abstract fun discoverMoviesDao(): DiscoverMoviesDao

  abstract fun myShowsDao(): MyShowsDao

  abstract fun myMoviesDao(): MyMoviesDao

  abstract fun watchlistShowsDao(): WatchlistShowsDao

  abstract fun watchlistMoviesDao(): WatchlistMoviesDao

  abstract fun archiveShowsDao(): ArchiveShowsDao

  abstract fun archiveMoviesDao(): ArchiveMoviesDao

  abstract fun relatedShowsDao(): RelatedShowsDao

  abstract fun relatedMoviesDao(): RelatedMoviesDao

  abstract fun showImagesDao(): ShowImagesDao

  abstract fun movieImagesDao(): MovieImagesDao

  abstract fun recentSearchDao(): RecentSearchDao

  abstract fun episodesDao(): EpisodesDao

  abstract fun seasonsDao(): SeasonsDao

  abstract fun peopleDao(): PeopleDao

  abstract fun peopleShowsMoviesDao(): PeopleShowsMoviesDao

  abstract fun peopleCreditsDao(): PeopleCreditsDao

  abstract fun peopleImagesDao(): PeopleImagesDao

  abstract fun settingsDao(): SettingsDao


  abstract fun moviesSyncLogDao(): MoviesSyncLogDao

  abstract fun episodesSyncLogDao(): EpisodesSyncLogDao

  abstract fun translationsSyncLogDao(): TranslationsSyncLogDao

  abstract fun translationsMoviesSyncLogDao(): TranslationsMoviesSyncLogDao

  abstract fun showTranslationsDao(): ShowTranslationsDao

  abstract fun movieTranslationsDao(): MovieTranslationsDao

  abstract fun ratingsDao(): RatingsDao

  abstract fun showRatingsDao(): ShowRatingsDao

  abstract fun movieRatingsDao(): MovieRatingsDao

  abstract fun showStreamingsDao(): ShowStreamingsDao

  abstract fun movieStreamingsDao(): MovieStreamingsDao

  abstract fun movieCollectionsDao(): MovieCollectionsDao

  abstract fun movieCollectionsItemsDao(): MovieCollectionsItemsDao

  abstract fun episodeTranslationsDao(): EpisodeTranslationsDao

  abstract fun customListsDao(): CustomListsDao

  abstract fun customListsItemsDao(): CustomListsItemsDao

  abstract fun floppySyncQueueDao(): FloppySyncQueueDao
}
