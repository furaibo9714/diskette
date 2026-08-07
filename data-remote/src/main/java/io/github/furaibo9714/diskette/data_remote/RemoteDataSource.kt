package io.github.furaibo9714.diskette.data_remote

import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.trakt.TraktRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides external data sources access points.
 */
interface RemoteDataSource {
  val trakt: TraktRemoteDataSource
  val tmdb: TmdbRemoteDataSource
}

@Singleton
internal class MainRemoteDataSource @Inject constructor(
  override val trakt: TraktRemoteDataSource,
  override val tmdb: TmdbRemoteDataSource,
) : RemoteDataSource
