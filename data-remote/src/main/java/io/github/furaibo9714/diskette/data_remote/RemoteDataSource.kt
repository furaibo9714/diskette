package io.github.furaibo9714.diskette.data_remote

import io.github.furaibo9714.diskette.data_remote.media.MediaRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.tmdb.TmdbRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides external data sources access points.
 */
interface RemoteDataSource {
  val media: MediaRemoteDataSource
  val tmdb: TmdbRemoteDataSource
}

@Singleton
internal class MainRemoteDataSource @Inject constructor(
  override val media: MediaRemoteDataSource,
  override val tmdb: TmdbRemoteDataSource,
) : RemoteDataSource
