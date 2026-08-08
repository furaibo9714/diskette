package io.github.furaibo9714.diskette.ui_backup.features.import_.runners

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.common.extensions.toUtcDateTime
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.ArchiveMovie
import io.github.furaibo9714.diskette.data_local.database.model.MyMovie
import io.github.furaibo9714.diskette.data_local.database.model.Rating
import io.github.furaibo9714.diskette.data_local.database.model.WatchlistMovie
import io.github.furaibo9714.diskette.repository.PinnedItemsRepository
import io.github.furaibo9714.diskette.repository.movies.MoviesRepository
import io.github.furaibo9714.diskette.repository.movies.ratings.MoviesRatingsRepository
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Importing
import io.github.furaibo9714.diskette.ui_backup.model.BackupMovie
import io.github.furaibo9714.diskette.ui_backup.model.BackupMovies
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.rethrowCancellation
import io.github.furaibo9714.diskette.ui_model.MediaId
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

internal class BackupImportMoviesRunner @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val moviesRepository: MoviesRepository,
  private val ratingsRepository: MoviesRatingsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
) : BackupImportRunner<BackupMovies>() {

  private var importedCount = 0
  private var importedTotal = 0

  override suspend fun run(backup: BackupMovies) {
    Timber.d("Initialized.")
    runImport(backup)
      .also {
        Timber.d("Success.")
      }
  }

  private suspend fun runImport(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      importMoviesCollection(backup)
      importMoviesPinned(backup)
      importMoviesRatings(backup)
    }
  }

  private suspend fun importMoviesPinned(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      val localPinned = pinnedItemsRepository.getAllMovies().map { it.key }
      for (pinned in backup.progressPinned) {
        if (!localPinned.contains(pinned)) {
          pinnedItemsRepository.addMoviePinnedItem(MediaId.parse(pinned))
        }
      }
    }
  }

  private suspend fun importMoviesRatings(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      val localRatings = ratingsRepository.loadMoviesRatings()

      for (rating in backup.ratingsMovies) {
        if (localRatings.any { it.mediaId.key == rating.mediaId }) {
          continue
        }

        val entity = Rating(
          mediaId = rating.mediaId,
          type = "movie",
          rating = rating.rating,
          seasonNumber = null,
          episodeNumber = null,
          ratedAt = rating.ratedAt.toUtcDateTime() ?: nowUtc(),
          createdAt = nowUtc(),
          updatedAt = nowUtc(),
        )

        localSource.ratings.replace(entity)
      }
    }
  }

  private suspend fun importMoviesCollection(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      val localCollection = moviesRepository
        .loadCollection()
        .map { it.mediaId.key }

      importedCount = 0
      importedTotal = backup.collectionHistory.size + backup.collectionWatchlist.size + backup.collectionHidden.size

      importMyMovies(backup, localCollection)
      importWatchlistMovies(backup, localCollection)
      importHiddenMovies(backup, localCollection)
    }
  }

  private suspend fun importMyMovies(
    backupMovies: BackupMovies,
    localCollection: List<String>,
  ) {
    for (movie in backupMovies.collectionHistory) {
      Timber.d("Importing movie ${movie.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(movie.title, importedCount, importedTotal))

      if (localCollection.contains(movie.mediaId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.mediaId)
      if (movieDetails == null) {
        if (!fetchMovieDetails(movie)) {
          continue
        }
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val myMovie = MyMovie.fromMediaId(movie.mediaId, timestamp)
      localSource.myMovies.insert(listOf(myMovie))

      Timber.d("Added to history ${movie.mediaId} ...")
    }
  }

  private suspend fun importWatchlistMovies(
    backupMovies: BackupMovies,
    localCollection: List<String>,
  ) {
    for (movie in backupMovies.collectionWatchlist) {
      Timber.d("Importing movie ${movie.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(movie.title, importedCount, importedTotal))

      if (localCollection.contains(movie.mediaId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.mediaId)
      if (movieDetails == null) {
        if (!fetchMovieDetails(movie)) {
          continue
        }
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val watchlistMovie = WatchlistMovie.fromMediaId(movie.mediaId, timestamp)
      localSource.watchlistMovies.insert(watchlistMovie)

      Timber.d("Added to Watchlist ${movie.mediaId} ...")
    }
  }

  private suspend fun importHiddenMovies(
    backupMovies: BackupMovies,
    localCollection: List<String>,
  ) {
    for (movie in backupMovies.collectionHidden) {
      Timber.d("Importing movie ${movie.mediaId} ...")
      importedCount++
      statusListener?.invoke(Importing(movie.title, importedCount, importedTotal))

      if (localCollection.contains(movie.mediaId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.mediaId)
      if (movieDetails == null) {
        if (!fetchMovieDetails(movie)) {
          continue
        }
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val hiddenMovie = ArchiveMovie.fromMediaId(movie.mediaId, timestamp)
      localSource.archiveMovies.insert(hiddenMovie)

      Timber.d("Added to Hidden ${movie.mediaId} ...")
    }
  }

  private suspend fun fetchMovieDetails(movie: BackupMovie): Boolean {
    Timber.d("Fetching remote movie details for ${movie.mediaId} ...")
    return try {
      moviesRepository.movieDetails.load(MediaId.parse(movie.mediaId), force = true)
      true
    } catch (error: Throwable) {
      rethrowCancellation(error) {
        if (error is HttpException && error.code() == 404) {
          Timber.w("Failed to fetch movie: ${movie.mediaId} ${movie.title}")
        }
      }
      false
    }
  }
}
