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
