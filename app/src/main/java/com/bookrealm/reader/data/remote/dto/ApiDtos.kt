package com.bookrealm.reader.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val code: Int = 0,
    val data: T? = null,
    val message: String = "",
)

@Serializable
data class UserLoginRequest(
    val userAccount: String,
    val userPassword: String,
    val loginType: String = "App",
)

@Serializable
data class LoginUserResponse(
    val token: String = "",
    val user: UserDto = UserDto(),
)

@Serializable
data class UserDto(
    val id: Long = 0,
    val userAccount: String = "",
    val username: String? = null,
    val avatarUrl: String? = null,
    val gender: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val userRole: Int = 0,
)

@Serializable
data class BookListResponse(
    val items: List<BookItemDto> = emptyList(),
    val total: Long = 0,
    val page: Int = 1,
    val size: Int = 20,
)

@Serializable
data class BookItemDto(
    val id: Long = 0,
    val title: String = "",
    val author: String = "",
    val coverUrl: String? = null,
    val intro: String = "",
    val tags: List<String> = emptyList(),
)

@Serializable
data class BookDetailDto(
    val id: Long = 0,
    val title: String = "",
    val author: String = "",
    val coverUrl: String? = null,
    val intro: String = "",
    val tags: List<String> = emptyList(),
    val chapters: List<ChapterItemDto> = emptyList(),
)

@Serializable
data class ChapterItemDto(
    val id: Long = 0,
    val seq: Int = 0,
    val title: String = "",
)

@Serializable
data class ChapterDetailDto(
    val id: Long = 0,
    val bookId: Long = 0,
    val seq: Int = 0,
    val title: String = "",
    val paragraphs: List<ParagraphDto> = emptyList(),
)

@Serializable
data class ParagraphDto(
    val id: Long = 0,
    val seq: Int = 0,
    val content: String = "",
)

@Serializable
data class ReadingProgressRequest(
    val userId: Long,
    val bookId: Long,
    val chapterId: Long,
    val paragraphIndex: Int,
)

@Serializable
data class ReadingStatsResponse(
    val date: String = "",
    val userId: Long = 0,
    val bookId: Long = 0,
    val chapterId: Long = 0,
    val paragraphIndex: Int = 0,
    val reportCount: Int = 0,
    val lastReportTime: String = "",
)

@Serializable
data class AiSummaryRequest(val chapterText: String)

@Serializable
data class AiSummaryResponse(
    val summary: String = "",
    val llmUsed: Boolean = false,
    val message: String = "",
)

@Serializable
data class AiEmbedRequest(val bookId: Long)

@Serializable
data class AiEmbedResponse(
    val bookId: Long = 0,
    val documentCount: Int = 0,
)

@Serializable
data class AiAskRequest(
    val bookId: Long,
    val chapterId: Long? = null,
    val question: String,
    val selectedText: String? = null,
)

@Serializable
data class AiReferenceDto(
    val bookId: Long = 0,
    val chapterId: Long = 0,
    val paragraphSeq: Int = 0,
    val content: String = "",
)

@Serializable
data class AiAskResponse(
    val answer: String = "",
    val llmUsed: Boolean = false,
    val references: List<AiReferenceDto> = emptyList(),
    val message: String = "",
)
