package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.ConfigVariant.COLLECTIONS_CACHE_DURATION
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.toMillis
import io.github.furaibo9714.diskette.data_local.database.model.MovieCollectionItem
import io.github.furaibo9714.diskette.data_local.sources.MovieCollectionsItemsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MovieCollectionsLocalDataSource
import io.github.furaibo9714.diskette.data_local.sources.MoviesLocalDataSource
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.CollectionMapper
import io.github.furaibo9714.diskette.repository.mappers.MovieMapper
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Movie
import io.github.furaibo9714.diskette.ui_model.MovieCollection
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class MovieCollectionsRepository @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: MediaRemoteDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val movieCollectionsLocalSource: MovieCollectionsLocalDataSource,
  private val movieCollectionsItemsLocalSource: MovieCollectionsItemsLocalDataSource,
  private val collectionMapper: CollectionMapper,
  private val movieMapper: MovieMapper,
  private val transactions: TransactionsProvider,
) {

  suspend fun loadCollection(collectionId: MediaId) =
    withContext(dispatchers.IO) {
      movieCollectionsLocalSource.getById(collectionId.key)
    }

  suspend fun loadCollections(movieId: MediaId): Pair<List<MovieCollection>, Source> =
    withContext(dispatchers.IO) {
      val now = nowUtc()
      val localCollections = movieCollectionsLocalSource.getByMovieId(movieId.key)

      val localTimestamp = localCollections.firstOrNull()?.updatedAt
      localTimestamp?.let { timestamp ->
        if (now.toMillis() - timestamp.toMillis() < COLLECTIONS_CACHE_DURATION) {
          return@withContext Pair(
            localCollections.map { collectionMapper.fromEntity(it) },
            Source.LOCAL,
          )
        }
      }

      val remoteCollections = remoteSource.fetchMovieCollections(movieId.tmdbIdOrNull ?: return@withContext Pair(emptyList(), Source.REMOTE))
      val collections = remoteCollections.map { collectionMapper.fromNetwork(it) }

      updateLocalCollections(collections, movieId, now)

      return@withContext Pair(
        collections,
        Source.REMOTE,
      )
    }

  suspend fun loadCollectionItems(collectionId: MediaId): List<Movie> =
    withContext(dispatchers.IO) {
      val now = nowUtc()
      val localItems = movieCollectionsItemsLocalSource.getById(collectionId.key)

      val localTimestamp = localItems.firstOrNull()?.updatedAt
      localTimestamp?.let { timestamp ->
        if (now.toMillis() - timestamp < COLLECTIONS_CACHE_DURATION) {
          return@withContext localItems.map { movieMapper.fromDatabase(it) }
        }
      }

      val remoteItems = remoteSource.fetchMovieCollectionItems(collectionId.tmdbIdOrNull ?: return@withContext emptyList())
      val items = remoteItems.map { movieMapper.fromNetwork(it) }

      transactions.withTransaction {
        val entities = items.mapIndexed { index, movie ->
          MovieCollectionItem(
            rank = index,
            mediaId = movie.mediaId.key,
            collectionMediaId = collectionId.key,
            createdAt = now,
            updatedAt = now,
          )
        }
        moviesLocalSource.upsert(items.map { movieMapper.toDatabase(it) })
        movieCollectionsItemsLocalSource.replace(collectionId.key, entities)

        // Fill up collection with other movies that belong in it.
        val collection = movieCollectionsLocalSource.getById(collectionId.key)
        collection?.let { coll ->
          val insertEntities = entities
            .filter { it.mediaId != coll.movieMediaId }
            .map { coll.copy(id = 0, movieMediaId = it.mediaId) }
          movieCollectionsLocalSource.insertAll(insertEntities)
        }
      }

      return@withContext items
    }

  private suspend fun updateLocalCollections(
    collections: List<MovieCollection>,
    movieId: MediaId,
    now: ZonedDateTime,
  ) {
    var entities = collections.map {
      collectionMapper.toEntity(
        movieId = movieId.key,
        input = it,
        updatedAt = now,
        createdAt = now,
      )
    }
    if (entities.isEmpty()) {
      entities = listOf(
        collectionMapper.toEntity(
          movieId.key,
          MovieCollection.EMPTY,
        ),
      )
    }
    movieCollectionsLocalSource.replaceByMovieId(movieId.key, entities)
  }

  enum class Source { LOCAL, REMOTE }
}
