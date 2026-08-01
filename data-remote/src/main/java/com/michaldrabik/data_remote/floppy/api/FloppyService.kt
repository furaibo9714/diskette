package com.michaldrabik.data_remote.floppy.api

import com.michaldrabik.data_remote.floppy.model.FloppyInfo
import com.michaldrabik.data_remote.floppy.model.FloppyMedia
import com.michaldrabik.data_remote.floppy.model.FloppyMediaDetail
import com.michaldrabik.data_remote.floppy.model.FloppyMediaListResponse
import com.michaldrabik.data_remote.floppy.model.FloppyStatusUpdateRequest
import com.michaldrabik.data_remote.floppy.model.FloppyTrackRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FloppyService {

  @GET("api/v1/health/")
  suspend fun health()

  @GET("api/v1/info/")
  suspend fun info(): FloppyInfo

  @GET("api/v1/media/{mediaType}/")
  suspend fun getTrackedMedia(
    @Path("mediaType") mediaType: String,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
  ): FloppyMediaListResponse

  @GET("api/v1/media/{mediaType}/{source}/{mediaId}/")
  suspend fun getMediaDetail(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
  ): FloppyMediaDetail

  @POST("api/v1/media/{mediaType}/")
  suspend fun trackMedia(
    @Path("mediaType") mediaType: String,
    @Body body: FloppyTrackRequest,
  ): FloppyMedia

  @DELETE("api/v1/media/{mediaType}/{source}/{mediaId}/")
  suspend fun untrackMedia(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
  )

  @PATCH("api/v1/media/{mediaType}/{source}/{mediaId}/")
  suspend fun updateStatus(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Body body: FloppyStatusUpdateRequest,
  ): FloppyMedia

  @POST("api/v1/media/tv/{source}/{mediaId}/{seasonNumber}/episodes/{episodeNumber}/watch/")
  suspend fun watchEpisode(
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int,
  ): FloppyMedia

  @DELETE("api/v1/media/tv/{source}/{mediaId}/{seasonNumber}/episodes/{episodeNumber}/watch/")
  suspend fun unwatchEpisode(
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int,
  )

  companion object {
    const val MEDIA_LIST_PAGE_SIZE = 100
  }
}
