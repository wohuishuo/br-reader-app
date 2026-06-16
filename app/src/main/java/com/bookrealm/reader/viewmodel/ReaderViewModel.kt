package com.bookrealm.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.CommentItemDto
import com.bookrealm.reader.data.remote.dto.MarkItemDto
import com.bookrealm.reader.data.repository.ReaderRepository
import com.bookrealm.reader.data.repository.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val session: SessionSnapshot = SessionSnapshot(),
    val books: UiState<List<BookItemDto>> = UiState.Loading,
    val shelf: List<BookCacheEntity> = emptyList(),
    val selectedBook: UiState<BookDetailDto>? = null,
    val selectedChapter: UiState<ChapterDetailDto>? = null,
    val readingFromShelf: Boolean = false,
    val chapterMarks: List<MarkItemDto> = emptyList(),
    val paragraphComments: List<CommentItemDto> = emptyList(),
    val activeInteractionParagraphId: Long? = null,
    val query: String = "",
    val aiResult: String? = null,
    val darkTheme: Boolean = true,
    val dynamicColor: Boolean = false,
    val themePreset: String = "purple",
    val notice: String? = null,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: ReaderRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(ReaderUiState())

    val uiState: StateFlow<ReaderUiState> = combine(
        mutable,
        repository.session,
        repository.shelfBooks,
    ) { state, session, shelf ->
        state.copy(session = session, shelf = shelf)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState())

    init {
        refreshBooks()
    }

    fun login(account: String, password: String) = viewModelScope.launch {
        runCatching { repository.login(account, password) }
            .onSuccess { setNotice("登录成功") }
            .onFailure { setNotice(it.toUserMessage()) }
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        mutable.value = mutable.value.copy(selectedBook = null, selectedChapter = null)
    }

    fun refreshBooks(query: String = mutable.value.query) = viewModelScope.launch {
        mutable.value = mutable.value.copy(books = UiState.Loading, query = query)
        runCatching { repository.searchBooks(query) }
            .onSuccess { mutable.value = mutable.value.copy(books = UiState.Success(it)) }
            .onFailure { mutable.value = mutable.value.copy(books = UiState.Error(it.toUserMessage())) }
    }

    fun openBook(bookId: Long) = viewModelScope.launch {
        mutable.value = mutable.value.copy(selectedBook = UiState.Loading, selectedChapter = null, readingFromShelf = false)
        runCatching { repository.bookDetail(bookId) }
            .onSuccess { mutable.value = mutable.value.copy(selectedBook = UiState.Success(it)) }
            .onFailure { mutable.value = mutable.value.copy(selectedBook = UiState.Error(it.toUserMessage())) }
    }

    fun openBookForReading(bookId: Long) = viewModelScope.launch {
        val session = uiState.value.session
        if (session.lastBookId == bookId && session.lastChapterId > 0) {
            mutable.value = mutable.value.copy(readingFromShelf = true)
            openChapterDirect(bookId, session.lastChapterId)
            return@launch
        }
        mutable.value = mutable.value.copy(selectedBook = UiState.Loading, selectedChapter = null, readingFromShelf = true)
        runCatching { repository.bookDetail(bookId) }
            .onSuccess { detail ->
                val firstChapter = detail.chapters.firstOrNull()
                if (firstChapter != null) {
                    mutable.value = mutable.value.copy(selectedBook = null)
                    openChapterDirect(detail.id, firstChapter.id)
                } else {
                    setNotice("这本书还没有章节")
                }
            }
            .onFailure { mutable.value = mutable.value.copy(selectedBook = UiState.Error(it.toUserMessage())) }
    }

    fun closeBook() {
        mutable.value = mutable.value.copy(selectedBook = null, selectedChapter = null)
    }

    fun addToShelf(book: BookDetailDto) = viewModelScope.launch {
        runCatching { repository.addToShelf(book) }
            .onSuccess { setNotice("已加入书架") }
            .onFailure { setNotice(it.toUserMessage()) }
    }

    fun removeFromShelf(bookId: Long) = viewModelScope.launch {
        runCatching { repository.removeFromShelf(bookId) }
            .onSuccess { setNotice("已移出书架") }
            .onFailure { setNotice(it.toUserMessage()) }
    }

    fun openChapter(bookId: Long, chapterId: Long) = viewModelScope.launch {
        mutable.value = mutable.value.copy(readingFromShelf = false)
        openChapterInternal(bookId, chapterId, keepDetail = true)
    }

    private fun openChapterDirect(bookId: Long, chapterId: Long) = viewModelScope.launch {
        openChapterInternal(bookId, chapterId, keepDetail = false)
    }

    private suspend fun openChapterInternal(bookId: Long, chapterId: Long, keepDetail: Boolean) {
        mutable.value = mutable.value.copy(
            selectedBook = if (keepDetail) mutable.value.selectedBook else null,
            selectedChapter = UiState.Loading,
        )
        runCatching { repository.chapterDetail(chapterId) }
            .onSuccess {
                val userId = effectiveUserId()
                repository.saveProgress(userId, bookId, chapterId, 0)
                val marks = repository.chapterMarks(userId, chapterId)
                mutable.value = mutable.value.copy(selectedChapter = UiState.Success(it), chapterMarks = marks)
            }
            .onFailure { mutable.value = mutable.value.copy(selectedChapter = UiState.Error(it.toUserMessage())) }
    }

    fun closeChapter() {
        val returnToShelf = mutable.value.readingFromShelf
        mutable.value = mutable.value.copy(
            selectedChapter = null,
            selectedBook = if (returnToShelf) null else mutable.value.selectedBook,
            readingFromShelf = false,
            chapterMarks = emptyList(),
            paragraphComments = emptyList(),
            activeInteractionParagraphId = null,
        )
    }

    fun saveParagraphMark(paragraphId: Long, paragraphSeq: Int, note: String? = null) = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        val userId = effectiveUserId()
        if (userId <= 0) {
            setNotice("登录后才能保存划线和笔记")
            return@launch
        }
        runCatching {
            repository.saveMark(userId, chapter.bookId, chapter.id, paragraphId, paragraphSeq, note)
            repository.chapterMarks(userId, chapter.id)
        }.onSuccess {
            mutable.value = mutable.value.copy(chapterMarks = it)
            setNotice(if (note.isNullOrBlank()) "已划线" else "笔记已保存")
        }.onFailure {
            setNotice(it.toUserMessage())
        }
    }

    fun openParagraphInteraction(paragraphId: Long) = viewModelScope.launch {
        val userId = effectiveUserId()
        if (userId <= 0) {
            setNotice("登录后才能查看和参与段评")
            return@launch
        }
        runCatching { repository.paragraphInteraction(userId, paragraphId) }
            .onSuccess {
                mutable.value = mutable.value.copy(
                    activeInteractionParagraphId = paragraphId,
                    paragraphComments = it.comments,
                )
            }
            .onFailure { setNotice(it.toUserMessage()) }
    }

    fun closeParagraphInteraction() {
        mutable.value = mutable.value.copy(activeInteractionParagraphId = null, paragraphComments = emptyList())
    }

    fun saveParagraphComment(paragraphId: Long, content: String) = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        val userId = effectiveUserId()
        if (userId <= 0) {
            setNotice("登录后才能发布段评")
            return@launch
        }
        runCatching {
            repository.saveComment(userId, chapter.bookId, chapter.id, paragraphId, content)
            repository.paragraphInteraction(userId, paragraphId)
        }.onSuccess {
            mutable.value = mutable.value.copy(
                activeInteractionParagraphId = paragraphId,
                paragraphComments = it.comments,
            )
            setNotice("段评已发布")
        }.onFailure {
            setNotice(it.toUserMessage())
        }
    }

    fun toggleCommentLike(comment: CommentItemDto) = viewModelScope.launch {
        val userId = effectiveUserId()
        runCatching { repository.toggleCommentLike(userId, comment) }
            .onSuccess { updated ->
                mutable.value = mutable.value.copy(
                    paragraphComments = mutable.value.paragraphComments.map {
                        if (it.id == updated.id) updated else it
                    }
                )
            }
            .onFailure { setNotice(it.toUserMessage()) }
    }

    fun setFontScale(scale: Float) = viewModelScope.launch {
        repository.saveFontScale(scale)
    }

    fun setLineScale(scale: Float) = viewModelScope.launch {
        repository.saveLineScale(scale)
    }

    fun setReaderPalette(palette: String) = viewModelScope.launch {
        repository.saveReaderPalette(palette)
    }

    fun deleteParagraphMark(markId: Long) = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        val userId = effectiveUserId()
        if (userId <= 0) {
            setNotice("登录后才能删除划线")
            return@launch
        }
        runCatching {
            repository.deleteMark(userId, markId)
            repository.chapterMarks(userId, chapter.id)
        }.onSuccess {
            mutable.value = mutable.value.copy(chapterMarks = it)
            setNotice("划线已删除")
        }.onFailure {
            setNotice(it.toUserMessage())
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        mutable.value = mutable.value.copy(darkTheme = enabled)
    }

    fun setDynamicColor(enabled: Boolean) {
        mutable.value = mutable.value.copy(dynamicColor = enabled)
    }

    fun setThemePreset(preset: String) {
        mutable.value = mutable.value.copy(themePreset = preset, dynamicColor = false)
    }

    fun saveProgress(userId: Long, bookId: Long, chapterId: Long, paragraphIndex: Int) = viewModelScope.launch {
        repository.saveProgress(userId, bookId, chapterId, paragraphIndex)
    }

    fun summarizeCurrentChapter() = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        mutable.value = mutable.value.copy(aiResult = "正在生成摘要...")
        runCatching { repository.summarize(chapter) }
            .onSuccess { mutable.value = mutable.value.copy(aiResult = "摘要: $it") }
            .onFailure { mutable.value = mutable.value.copy(aiResult = it.toUserMessage()) }
    }

    fun askCurrentChapter(question: String) = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        if (question.isBlank()) {
            setNotice("先输入一个问题")
            return@launch
        }
        mutable.value = mutable.value.copy(aiResult = "正在检索原文...")
        runCatching { repository.ask(chapter.bookId, chapter.id, question) }
            .onSuccess { answer ->
                mutable.value = mutable.value.copy(aiResult = answer.toReadableAnswer())
            }
            .onFailure { mutable.value = mutable.value.copy(aiResult = it.toUserMessage()) }
    }

    fun consumeNotice() {
        mutable.value = mutable.value.copy(notice = null)
    }

    private fun setNotice(message: String) {
        mutable.value = mutable.value.copy(notice = message)
    }

    private fun effectiveUserId(): Long {
        val session = uiState.value.session
        return when {
            session.userId > 0 -> session.userId
            session.token.isNotBlank() -> 1L
            else -> 0L
        }
    }

    private fun com.bookrealm.reader.data.remote.dto.AiAskResponse.toReadableAnswer(): String {
        val cited = references.take(3).joinToString("、") { "第${it.paragraphSeq}段" }.ifBlank { "暂无引用" }
        val firstBasis = references.firstOrNull()?.content?.take(90).orEmpty()
        val cleanAnswer = answer
            .replace(Regex("已检索到.*?依据[:：]?"), "")
            .trim()
            .ifBlank { message.ifBlank { "暂时没有生成回答。" } }
        return if (llmUsed) {
            "$cleanAnswer\n\n依据：$cited"
        } else {
            "模型还没接入,先按原文给出可读结论：\n$cleanAnswer\n\n依据：$cited${if (firstBasis.isNotBlank()) "\n原文线索：$firstBasis..." else ""}"
        }
    }
}
