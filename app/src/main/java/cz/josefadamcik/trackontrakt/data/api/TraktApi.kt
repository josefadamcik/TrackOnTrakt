
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

/**
 * Trakt API interface for retrofit.
 *
 * http://docs.trakt.apiary.io/
 *
 * Contains only calls need for the application.
 */
interface TraktApi {
    enum class ExtendedInfo {
        full, metadata, episodes
    }

    @POST("/oauth/token")
    fun oauthToken(@Body data: OauthTokenRequest): Single<Response<OauthTokenResponse>>

    @GET("/users/settings")
    fun userSettings(@Header("Authorization") authorization: String): Single<Settings>


    @GET("/users/me/history")
    fun myHistory(@Header("Authorization") authorization: String,
                  @Query("page") page: Int = 1,
                  @Query("limit") limit: Int = 20)
        : Single<Response<List<HistoryItem>>>

    @GET("/users/me/watching")
    fun watching(@Header("Authorization") authorization: String,
                 @Query("extended") extended: ExtendedInfo = ExtendedInfo.metadata
    )
        : Single<Response<Watching.Something>>

    @GET("/search/{type}")
    fun search(@Header("Authorization") authorization: String,
               @Path("type") type: String,
               @Query("query") query: String,
               @Query("extended") extended: ExtendedInfo = ExtendedInfo.metadata,
               @Query("page") page: Int = 1,
               @Query("limit") limit: Int = 50)
        : Single<List<SearchResultItem>>

    @GET("/movies/{id}")
    fun movie(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Query("extended") extended: ExtendedInfo = ExtendedInfo.full
    ): Single<MovieDetail>

    @GET("/shows/{id}")
    fun show(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Query("extended") extended: ExtendedInfo = ExtendedInfo.full
    ): Single<ShowDetail>

    @GET("/shows/{id}/progress/watched")
    fun showWatchedProgress(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Query("hidden") hidden: Boolean = false,
        @Query("specials") specials: Boolean = false,
        @Query("count_specials") countSpecials: Boolean = false
    ): Single<Response<ShowWatchedProgress>>

    @GET("/shows/{id}/last_episode")
    fun showLastEpisode(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Query("extended") extended: ExtendedInfo = ExtendedInfo.full
    ): Single<Response<Episode>>

    @GET("/shows/{id}/seasons")
    fun showSeasons(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Query("extended") extended: ExtendedInfo = ExtendedInfo.metadata
    ): Single<Response<List<Season>>>

    @GET("/shows/{id}/seasons/{season}")
    fun showSeasonEpisodes(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Path("season") season: Int,
        @Query("extended") extended: ExtendedInfo = ExtendedInfo.metadata

    ): Single<Response<List<Episode>>>

    @POST("/checkin")
    fun checkin(@Header("Authorization") authorization: String,
                @Body data: CheckinRequest
    ): Single<Response<CheckinResponse>>

    companion object {
        public const val HEADER_PAGINATION_PAGE = "X-Pagination-Page"
        public const val HEADER_PAGINATION_PAGE_COUNT = "X-Pagination-Page-Count"
        public const val HEADER_PAGINATION_ITEM_COUNT = "X-Pagination-Item-Count"
    }
}