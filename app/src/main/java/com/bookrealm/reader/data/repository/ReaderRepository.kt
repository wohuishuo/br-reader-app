package com.bookrealm.reader.data.repository

import com.bookrealm.reader.data.local.BookCacheDao
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionStore
import com.bookrealm.reader.data.remote.LibraryApi
import com.bookrealm.reader.data.remote.UserCenterApi
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.UserLoginRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepository @Inject constructor(
    private val userCenterApi: UserCenterApi,
    private val libraryApi: LibraryApi,
    private val bookCacheDao: BookCacheDao,
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
            account = body.user.userAccount.ifBlank { account.trim() },
            username = body.user.username?.takeIf { it.isNotBlank() } ?: body.user.userAccount,
        )
    }

    suspend fun logout() = sessionStore.clearLogin()

    suspend fun searchBooks(query: String?): List<BookItemDto> {
        val books = libraryApi.listBooks(query = query?.takeIf { it.isNotBlank() }).requireData().items
        bookCacheDao.upsert(books.map { it.toCacheEntity(inShelf = false) })
        return books
    }

    suspend fun bookDetail(bookId: Long): BookDetailDto = libraryApi.bookDetail(bookId).requireData()

    suspend fun addToShelf(book: BookDetailDto) {
        bookCacheDao.upsert(listOf(book.toCacheEntity(inShelf = true)))
        bookCacheDao.markInShelf(book.id)
    }

    suspend fun chapterDetail(chapterId: Long): ChapterDetailDto =
        libraryApi.chapterDetail(chapterId).requireData()

    suspend fun saveFontScale(scale: Float) = sessionStore.saveFontScale(scale)

    suspend fun saveProgress(bookId: Long, chapterId: Long, paragraphIndex: Int) =
        sessionStore.saveProgress(bookId, chapterId, paragraphIndex)

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
