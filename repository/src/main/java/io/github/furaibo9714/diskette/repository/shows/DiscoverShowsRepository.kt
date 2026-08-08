package io.github.furaibo9714.diskette.repository.shows

import io.github.furaibo9714.diskette.common.Config
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.database.model.DiscoverShow
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
import io.github.furaibo9714.diskette.ui_model.Network
import io.github.furaibo9714.diskette.ui_model.Show
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DiscoverShowsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun isCacheValid(): Boolean {
    val stamp = localSource.discoverShows.getMostRecent()?.createdAt ?: 0
    return nowUtcMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION
  }

  suspend fun loadAllCached(): List<Show> {
    val cachedShows = localSource.discoverShows.getAll().map { it.mediaId }
    val shows = localSource.shows.getAll(cachedShows)

    return cachedShows
      .map { id -> shows.first { it.mediaId == id } }
      .map { mappers.show.fromDatabase(it) }
  }

  suspend fun loadAllRemote(
    order: DiscoverFeed,
    showCollection: Boolean,
    collectionSize: Int,
    genres: List<Genre>,
    networks: List<Network>,
  ): List<Show> =
    when (order) {
      TRENDING, RECENT -> loadRemoteTrending(genres, networks, showCollection, collectionSize)
      POPULAR -> loadRemotePopular(genres, networks)
      ANTICIPATED -> loadRemoteAnticipated(genres, networks)
    }

  private suspend fun loadRemoteTrending(
    genres: List<Genre>,
    networks: List<Network>,
    showCollection: Boolean,
    collectionSize: Int,
  ): List<Show> {
    return coroutineScope {
      val resultShows = mutableListOf<Show>()
      val genresQuery = genres.joinToString(",") { it.slug }
      val networksQuery = networks.joinToString(",") { it.channels.joinToString(",") }

      val limit =
        if (showCollection) {
          DISCOVER_LIMIT
        } else {
          DISCOVER_LIMIT + (collectionSize / 2)
        }

      val trendingShowsAsync = async {
        remoteSource.media
          .fetchTrendingShows(genresQuery, networksQuery, limit)
          .map { mappers.show.fromNetwork(it) }
      }

      val anticipatedShowsAsync = async {
        remoteSource.media
          .fetchAnticipatedShows(genresQuery, networksQuery, ANTICIPATED_LIMIT)
          .map { mappers.show.fromNetwork(it) }
      }

      val trendingShows = trendingShowsAsync.await()
      val anticipatedShows = anticipatedShowsAsync.await().toMutableList()

      trendingShows.forEachIndexed { index, trendingShow ->
        addIfMissing(resultShows, trendingShow)
        if (index != 0 && index % 6 == 0 && anticipatedShows.isNotEmpty()) {
          val anticipatedShow = anticipatedShows.removeAt(0)
          addIfMissing(resultShows, anticipatedShow)
        }
      }

      return@coroutineScope resultShows
    }
  }

  private suspend fun loadRemotePopular(
    genres: List<Genre>,
    networks: List<Network>,
  ): List<Show> {
    val genresQuery = genres.joinToString(",") { it.slug }
    val networksQuery = networks.joinToString(",") { it.channels.joinToString(",") }

    return remoteSource.media
      .fetchPopularShows(
        genres = genresQuery,
        networks = networksQuery,
        limit = DISCOVER_LIMIT,
      ).map { mappers.show.fromNetwork(it) }
  }

  private suspend fun loadRemoteAnticipated(
    genres: List<Genre>,
    networks: List<Network>,
  ): List<Show> {
    val genresQuery = genres.joinToString(",") { it.slug }
    val networksQuery = networks.joinToString(",") { it.channels.joinToString(",") }

    return remoteSource.media
      .fetchAnticipatedShows(
        genres = genresQuery,
        networks = networksQuery,
        limit = DISCOVER_LIMIT,
      ).map { mappers.show.fromNetwork(it) }
  }

  suspend fun cacheDiscoverShows(shows: List<Show>) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.shows.upsert(shows.map { mappers.show.toDatabase(it) })
      localSource.discoverShows.replace(
        shows.map {
          DiscoverShow(
            mediaId = it.ids.media.key,
            createdAt = timestamp,
            updatedAt = timestamp,
          )
        },
      )
    }
  }

  private fun addIfMissing(
    shows: MutableList<Show>,
    show: Show,
  ) {
    if (shows.any { it.ids.media == show.ids.media }) return
    shows.add(show)
  }
}
