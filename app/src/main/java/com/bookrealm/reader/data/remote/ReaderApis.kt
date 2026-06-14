package com.bookrealm.reader.data.remote

import com.bookrealm.reader.data.remote.dto.BaseResponse
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookListResponse
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.AiAskRequest
import com.bookrealm.reader.data.remote.dto.AiAskResponse
import com.bookrealm.reader.data.remote.dto.AiEmbedRequest
import com.bookrealm.reader.data.remote.dto.AiEmbedResponse
import com.bookrealm.reader.data.remote.dto.AiSummaryRequest
import com.bookrealm.reader.data.remote.dto.AiSummaryResponse
import com.bookrealm.reader.data.remote.dto.LoginUserResponse
import com.bookrealm.reader.data.remote.dto.ReadingProgressRequest
import com.bookrealm.reader.data.remote.dto.ReadingStatsResponse
import com.bookrealm.reader.data.remote.dto.UserLoginRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserCenterApi {
    @POST("user/login")
    suspend fun login(@Body request: UserLoginRequest): BaseResponse<LoginUserResponse>
}

interface LibraryApi {
    @GET("books")
    suspend fun listBooks(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): BaseResponse<BookListResponse>

    @GET("books/{id}")
    suspend fun bookDetail(@Path("id") id: Long): BaseResponse<BookDetailDto>

    @GET("chapters/{id}")
    suspend fun chapterDetail(@Path("id") id: Long): BaseResponse<ChapterDetailDto>
}

interface StatsApi {
    @POST("stats/progress")
    suspend fun reportProgress(@Body request: ReadingProgressRequest): BaseResponse<ReadingStatsResponse>
}

interface AiApi {
    @POST("ai/summary")
    suspend fun summary(@Body request: AiSummaryRequest): BaseResponse<AiSummaryResponse>

    @POST("ai/embed")
    suspend fun embed(@Body request: AiEmbedRequest): BaseResponse<AiEmbedResponse>

    @POST("ai/ask")
    suspend fun ask(@Body request: AiAskRequest): BaseResponse<AiAskResponse>
}
