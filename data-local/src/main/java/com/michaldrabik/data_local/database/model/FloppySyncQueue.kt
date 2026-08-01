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
  @ColumnInfo(name = "value") val value: String? = null,
) {

  companion object {
    fun createShowWatchlist(
      source: String,
      mediaId: String,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_TV, source, mediaId, null, null, Type.SHOW_WATCHLIST.slug, operation.slug, createdAt)

    fun createMovieWatchlist(
      source: String,
      mediaId: String,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_MOVIE, source, mediaId, null, null, Type.MOVIE_WATCHLIST.slug, operation.slug, createdAt)

    fun createMovieWatched(
      source: String,
      mediaId: String,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_MOVIE, source, mediaId, null, null, Type.MOVIE_WATCHED.slug, operation.slug, createdAt)

    fun createEpisodeWatched(
      source: String,
      showMediaId: String,
      seasonNumber: Int,
      episodeNumber: Int,
      operation: Operation,
      createdAt: Long,
    ) = FloppySyncQueue(
      0,
      MEDIA_TYPE_TV,
      source,
      showMediaId,
      seasonNumber,
      episodeNumber,
      Type.EPISODE_WATCHED.slug,
      operation.slug,
      createdAt,
    )

    fun createShowRating(
      source: String,
      mediaId: String,
      operation: Operation,
      score: Int?,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_TV, source, mediaId, null, null, Type.SHOW_RATING.slug, operation.slug, createdAt, score?.toString())

    fun createMovieRating(
      source: String,
      mediaId: String,
      operation: Operation,
      score: Int?,
      createdAt: Long,
    ) = FloppySyncQueue(0, MEDIA_TYPE_MOVIE, source, mediaId, null, null, Type.MOVIE_RATING.slug, operation.slug, createdAt, score?.toString())

    fun createSeasonRating(
      source: String,
      showMediaId: String,
      seasonNumber: Int,
      operation: Operation,
      score: Int?,
      createdAt: Long,
    ) = FloppySyncQueue(
      0,
      MEDIA_TYPE_TV,
      source,
      showMediaId,
      seasonNumber,
      null,
      Type.SEASON_RATING.slug,
      operation.slug,
      createdAt,
      score?.toString(),
    )

    fun createEpisodeRating(
      source: String,
      showMediaId: String,
      seasonNumber: Int,
      episodeNumber: Int,
      operation: Operation,
      score: Int?,
      createdAt: Long,
    ) = FloppySyncQueue(
      0,
      MEDIA_TYPE_TV,
      source,
      showMediaId,
      seasonNumber,
      episodeNumber,
      Type.EPISODE_RATING.slug,
      operation.slug,
      createdAt,
      score?.toString(),
    )

    const val MEDIA_TYPE_TV = "tv"
    const val MEDIA_TYPE_MOVIE = "movie"
    const val SOURCE_TMDB = "tmdb"
    const val SOURCE_MANUAL = "manual"
  }

  enum class Type(
    val slug: String,
  ) {
    SHOW_WATCHLIST("show_watchlist"),
    MOVIE_WATCHLIST("movie_watchlist"),
    MOVIE_WATCHED("movie_watched"),
    EPISODE_WATCHED("episode_watched"),
    SHOW_RATING("show_rating"),
    MOVIE_RATING("movie_rating"),
    SEASON_RATING("season_rating"),
    EPISODE_RATING("episode_rating"),
  }

  enum class Operation(
    val slug: String,
  ) {
    ADD("add"),
    REMOVE("remove"),
  }
}
