package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "floppy_sync_queue")
data class FloppySyncQueue(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
  @ColumnInfo(name = "media_type") val mediaType: String,
  @ColumnInfo(name = "source") val source: String,
  @ColumnInfo(name = "media_id") val mediaId: String,
  @ColumnInfo(name = "season_number") val seasonNumber: Int?,
  @ColumnInfo(name = "episode_number") val episodeNumber: Int?,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "operation") val operation: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
) {

  companion object {
    fun createShowWatchlist(
      idTmdb: Long,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_TV, SOURCE_TMDB, idTmdb.toString(), null, null, Type.SHOW_WATCHLIST.slug, operation.slug, createdAt)

    fun createMovieWatchlist(
      idTmdb: Long,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_MOVIE, SOURCE_TMDB, idTmdb.toString(), null, null, Type.MOVIE_WATCHLIST.slug, operation.slug, createdAt)

    fun createMovieWatched(
      idTmdb: Long,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_MOVIE, SOURCE_TMDB, idTmdb.toString(), null, null, Type.MOVIE_WATCHED.slug, operation.slug, createdAt)

    fun createEpisodeWatched(
      showIdTmdb: Long,
      seasonNumber: Int,
      episodeNumber: Int,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(
      0,
      MEDIA_TYPE_TV,
      SOURCE_TMDB,
      showIdTmdb.toString(),
      seasonNumber,
      episodeNumber,
      Type.EPISODE_WATCHED.slug,
      operation.slug,
      createdAt,
    )

    const val MEDIA_TYPE_TV = "tv"
    const val MEDIA_TYPE_MOVIE = "movie"
    const val SOURCE_TMDB = "tmdb"
  }

  enum class Type(
    val slug: String,
  ) {
    SHOW_WATCHLIST("show_watchlist"),
    MOVIE_WATCHLIST("movie_watchlist"),
    MOVIE_WATCHED("movie_watched"),
    EPISODE_WATCHED("episode_watched"),
  }

  enum class Operation(
    val slug: String,
  ) {
    ADD("add"),
    REMOVE("remove"),
  }
}
