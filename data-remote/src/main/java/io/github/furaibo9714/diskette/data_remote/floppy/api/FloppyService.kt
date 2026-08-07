package io.github.furaibo9714.diskette.data_remote.floppy.api

import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyDiscoverHiddenRequest
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyEmptyRequest
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyInfo
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyList
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyListCreateRequest
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMedia
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaDetail
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyMediaListResponse
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyScoreUpdateRequest
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyStatusUpdateRequest
import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyTrackRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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
  )

  @PATCH("api/v1/media/{mediaType}/{source}/{mediaId}/")
  suspend fun updateMediaScore(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Body body: FloppyScoreUpdateRequest,
  )

  @PATCH("api/v1/media/tv/{source}/{mediaId}/{seasonNumber}/")
  suspend fun updateSeasonScore(
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("seasonNumber") seasonNumber: Int,
    @Body body: FloppyScoreUpdateRequest,
  )

  @PATCH("api/v1/media/tv/{source}/{mediaId}/{seasonNumber}/episodes/{episodeNumber}/score/")
  suspend fun updateEpisodeScore(
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int,
    @Body body: FloppyScoreUpdateRequest,
  )

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

  @POST("api/v1/lists/")
  suspend fun createList(
    @Body body: FloppyListCreateRequest,
  ): FloppyList

  @PATCH("api/v1/lists/{listId}/")
  suspend fun updateList(
    @Path("listId") listId: Long,
    @Body body: FloppyListCreateRequest,
  ): FloppyList

  @DELETE("api/v1/lists/{listId}/")
  suspend fun deleteList(
    @Path("listId") listId: Long,
  )

  @PUT("api/v1/media/{mediaType}/{source}/{mediaId}/lists/{listId}/")
  suspend fun addToList(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("listId") listId: Long,
    @Body body: FloppyEmptyRequest,
  )

  @DELETE("api/v1/media/{mediaType}/{source}/{mediaId}/lists/{listId}/")
  suspend fun removeFromList(
    @Path("mediaType") mediaType: String,
    @Path("source") source: String,
    @Path("mediaId") mediaId: String,
    @Path("listId") listId: Long,
  )

  @POST("api/v1/discover/hidden/")
  suspend fun toggleDiscoverHidden(
    @Body body: FloppyDiscoverHiddenRequest,
  )

  companion object {
    const val MEDIA_LIST_PAGE_SIZE = 100
  }
}
