package com.bookrealm.reader.data.repository

import com.bookrealm.reader.data.local.BookCacheDao
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.ChapterCacheDao
import com.bookrealm.reader.data.local.ChapterCacheEntity
import com.bookrealm.reader.data.local.SessionStore
import com.bookrealm.reader.data.remote.AiApi
import com.bookrealm.reader.data.remote.LibraryApi
import com.bookrealm.reader.data.remote.StatsApi
import com.bookrealm.reader.data.remote.UserCenterApi
import com.bookrealm.reader.data.remote.dto.AiAskRequest
import com.bookrealm.reader.data.remote.dto.AiAskResponse
import com.bookrealm.reader.data.remote.dto.AiEmbedRequest
import com.bookrealm.reader.data.remote.dto.AiSummaryRequest
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.CommentItemDto
import com.bookrealm.reader.data.remote.dto.MarkItemDto
import com.bookrealm.reader.data.remote.dto.ParagraphInteractionDto
import com.bookrealm.reader.data.remote.dto.ReadingProgressRequest
import com.bookrealm.reader.data.remote.dto.SaveCommentRequest
import com.bookrealm.reader.data.remote.dto.SaveMarkRequest
import com.bookrealm.reader.data.remote.dto.UserLoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepository @Inject constructor(
    private val userCenterApi: UserCenterApi,
    private val libraryApi: LibraryApi,
    private val statsApi: StatsApi,
    private val aiApi: AiApi,
    private val bookCacheDao: BookCacheDao,
    private val chapterCacheDao: ChapterCacheDao,
    private val sessionStore: SessionStore,
) {
    val session = sessionStore.session
    val shelfBooks: Flow<List<BookCacheEntity>> = bookCacheDao.shelfBooks()

    suspend fun login(account: String, password: String) {
        val body = userCenterApi.login(
            UserLoginRequest(userAccount = account.trim(), userPassword = password, loginType = "App")
        ).requireData()
        sessionStore.saveLogin(
            token = body.token,
            userId = body.user.id,
            account = body.user.userAccount.ifBlank { account.trim() },
            username = body.user.username?.takeIf { it.isNotBlank() } ?: body.user.userAccount,
        )
    }

    suspend fun logout() = sessionStore.clearLogin()

    suspend fun searchBooks(query: String?): List<BookItemDto> {
        val books = libraryApi.listBooks(query = query?.takeIf { it.isNotBlank() }).requireData().items
        bookCacheDao.upsert(books.map { it.toCacheEntity(inShelf = bookCacheDao.isInShelf(it.id)) })
        return books
    }

    suspend fun bookDetail(bookId: Long): BookDetailDto = libraryApi.bookDetail(bookId).requireData()

    suspend fun addToShelf(book: BookDetailDto) {
        bookCacheDao.upsert(listOf(book.toCacheEntity(inShelf = true)))
        bookCacheDao.markInShelf(book.id)
        runCatching {
            book.chapters.take(8).forEach { chapter ->
                chapterDetail(chapter.id)
            }
        }
    }

    suspend fun chapterDetail(chapterId: Long): ChapterDetailDto {
        return runCatching {
            libraryApi.chapterDetail(chapterId).requireData().also { cacheChapter(it) }
        }.getOrElse { error ->
            chapterCacheDao.findById(chapterId)?.toDto() ?: throw error
        }
    }

    suspend fun chapterMarks(userId: Long, chapterId: Long): List<MarkItemDto> {
        if (userId <= 0) return emptyList()
        return libraryApi.chapterMarks(chapterId, userId).requireData()
    }

    suspend fun saveMark(
        userId: Long,
        bookId: Long,
        chapterId: Long,
        paragraphId: Long,
        paragraphSeq: Int,
        note: String?,
    ): MarkItemDto = libraryApi.saveMark(
        SaveMarkRequest(
            userId = userId,
            bookId = bookId,
            chapterId = chapterId,
            paragraphId = paragraphId,
            paragraphSeq = paragraphSeq,
            markType = if (note.isNullOrBlank()) "highlight" else "note",
            note = note,
        )
    ).requireData()

    suspend fun saveComment(
        userId: Long,
        bookId: Long,
        chapterId: Long,
        paragraphId: Long,
        content: String,
    ): CommentItemDto = libraryApi.saveComment(
        SaveCommentRequest(
            userId = userId,
            bookId = bookId,
            chapterId = chapterId,
            paragraphId = paragraphId,
            content = content,
        )
    ).requireData()

    suspend fun paragraphInteraction(userId: Long, paragraphId: Long): ParagraphInteractionDto {
        if (userId <= 0) return ParagraphInteractionDto(paragraphId = paragraphId)
        return libraryApi.paragraphInteraction(paragraphId, userId).requireData()
    }

    suspend fun toggleCommentLike(userId: Long, comment: CommentItemDto): CommentItemDto {
        if (userId <= 0) error("登录后才能点赞")
        return if (comment.likedByMe) {
            libraryApi.unlikeComment(comment.id, userId).requireData()
        } else {
            libraryApi.likeComment(comment.id, userId).requireData()
        }
    }

    suspend fun saveFontScale(scale: Float) = sessionStore.saveFontScale(scale)

    suspend fun saveProgress(userId: Long, bookId: Long, chapterId: Long, paragraphIndex: Int) {
        sessionStore.saveProgress(bookId, chapterId, paragraphIndex)
        if (userId > 0) {
            runCatching {
                statsApi.reportProgress(
                    ReadingProgressRequest(
                        userId = userId,
                        bookId = bookId,
                        chapterId = chapterId,
                        paragraphIndex = paragraphIndex,
                    )
                ).requireData()
            }
        }
    }

    suspend fun summarize(chapter: ChapterDetailDto): String {
        val text = chapter.paragraphs.joinToString("\n") { it.content }
        val response = aiApi.summary(AiSummaryRequest(text)).requireData()
        return response.summary.ifBlank { response.message }
    }

    suspend fun ask(bookId: Long, chapterId: Long, question: String): AiAskResponse {
        runCatching { aiApi.embed(AiEmbedRequest(bookId)).requireData() }
        return aiApi.ask(
            AiAskRequest(
                bookId = bookId,
                chapterId = chapterId,
                question = question,
            )
        ).requireData()
    }

    private fun BookItemDto.toCacheEntity(inShelf: Boolean) = BookCacheEntity(
        id = id,
        title = title,
        author = author,
        coverUrl = coverUrl,
        intro = intro,
        inShelf = inShelf,
    )

    private fun BookDetailDto.toCacheEntity(inShelf: Boolean) = BookCacheEntity(
        id = id,
        title = title,
        author = author,
        coverUrl = coverUrl,
        intro = intro,
        inShelf = inShelf,
    )

    private suspend fun cacheChapter(chapter: ChapterDetailDto) {
        chapterCacheDao.upsert(
            ChapterCacheEntity(
                id = chapter.id,
                bookId = chapter.bookId,
                seq = chapter.seq,
                title = chapter.title,
                paragraphsJson = Json.encodeToString(chapter.paragraphs),
                updateTime = System.currentTimeMillis(),
            )
        )
    }

    private fun ChapterCacheEntity.toDto() = ChapterDetailDto(
        id = id,
        bookId = bookId,
        seq = seq,
        title = title,
        paragraphs = Json.decodeFromString(paragraphsJson),
    )
}

private fun <T> com.bookrealm.reader.data.remote.dto.BaseResponse<T>.requireData(): T {
    if (code != 0 || data == null) {
        throw IllegalStateException(message.ifBlank { "请求失败: code=$code" })
    }
    return data
}

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "连接后端失败。请确认 start-platform.ps1 已启动，并已执行 adb reverse。"
    is HttpException -> "服务返回异常: HTTP ${code()}"
    else -> message ?: "操作失败"
}
