package io.github.furaibo9714.diskette.repository.movies

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.DiscoverMovie
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import io.github.furaibo9714.diskette.data_remote.Config.ANTICIPATED_LIMIT
import io.github.furaibo9714.diskette.data_remote.Config.DISCOVER_LIMIT
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import io.github.furaibo9714.diskette.repository.mappers.Mappers
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed.ANTICIPATED
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed.POPULAR
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed.RECENT
import io.github.furaibo9714.diskette.ui_model.DiscoverFeed.TRENDING
import io.github.furaibo9714.diskette.ui_model.Genre
import io.github.furaibo9714.diskette.ui_model.Movie
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DiscoverMoviesRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun isCacheValid(): Boolean {
    val stamp = localSource.discoverMovies.getMostRecent()?.createdAt ?: 0
    return nowUtcMillis() - stamp < Config.DISCOVER_MOVIES_CACHE_DURATION
  }

  suspend fun loadAllCached(): List<Movie> {
    val cachedMovies = localSource.discoverMovies.getAll().map { it.mediaId }
    val movies = localSource.movies.getAll(cachedMovies)

    return cachedMovies
      .map { id -> movies.first { it.mediaId == id } }
      .map { mappers.movie.fromDatabase(it) }
  }

  suspend fun loadAllRemote(
    order: DiscoverFeed,
    showCollection: Boolean,
    collectionSize: Int,
    genres: List<Genre>,
  ): List<Movie> =
    when (order) {
      TRENDING, RECENT -> loadRemoteTrending(genres, showCollection, collectionSize)
      POPULAR -> loadRemotePopular(genres)
      ANTICIPATED -> loadRemoteAnticipated(genres)
    }

  private suspend fun loadRemoteTrending(
    genres: List<Genre>,
    showCollection: Boolean,
    collectionSize: Int,
  ): List<Movie> {
    return coroutineScope {
      val resultMovies = mutableListOf<Movie>()
      val genresQuery = genres.joinToString(",") { it.slug }

      val limit =
        if (showCollection) {
          DISCOVER_LIMIT
        } else {
          DISCOVER_LIMIT + (collectionSize / 2)
        }

      val trendingMoviesAsync = async {
        remoteSource.media
          .fetchTrendingMovies(genresQuery, limit)
          .map { mappers.movie.fromNetwork(it) }
      }

      val anticipatedMoviesAsync = async {
        remoteSource.media
          .fetchAnticipatedMovies(genresQuery, ANTICIPATED_LIMIT)
          .map { mappers.movie.fromNetwork(it) }
      }

      val trendingMovies = trendingMoviesAsync.await()
      val anticipatedMovies = anticipatedMoviesAsync.await().toMutableList()

      trendingMovies.forEachIndexed { index, trendingMovie ->
        addIfMissing(resultMovies, trendingMovie)
        if (index != 0 && index % 6 == 0 && anticipatedMovies.isNotEmpty()) {
          val anticipatedMovie = anticipatedMovies.removeAt(0)
          addIfMissing(resultMovies, anticipatedMovie)
        }
      }

      return@coroutineScope resultMovies
    }
  }

  private suspend fun loadRemotePopular(genres: List<Genre>): List<Movie> {
    val genresQuery = genres.joinToString(",") { it.slug }
    return remoteSource.media
      .fetchPopularMovies(genresQuery, DISCOVER_LIMIT)
      .map { mappers.movie.fromNetwork(it) }
  }

  private suspend fun loadRemoteAnticipated(genres: List<Genre>): List<Movie> {
    val genresQuery = genres.joinToString(",") { it.slug }
    return remoteSource.media
      .fetchAnticipatedMovies(genresQuery, DISCOVER_LIMIT)
      .map { mappers.movie.fromNetwork(it) }
  }

  suspend fun cacheDiscoverMovies(movies: List<Movie>) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.movies.upsert(movies.map { mappers.movie.toDatabase(it) })
      localSource.discoverMovies.replace(
        movies.map {
          DiscoverMovie(
            mediaId = it.ids.media.id,
            createdAt = timestamp,
            updatedAt = timestamp,
          )
        },
      )
    }
  }

  private fun addIfMissing(
    movies: MutableList<Movie>,
    movie: Movie,
  ) {
    if (movies.any { it.ids.media == movie.ids.media }) {
      return
    }
    movies.add(movie)
  }
}
