package com.bookrealm.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
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
    val chapterMarks: List<MarkItemDto> = emptyList(),
    val query: String = "",
    val aiResult: String? = null,
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
            .onSuccess { setNotice("登录成功，欢迎回到书域") }
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
        mutable.value = mutable.value.copy(selectedBook = UiState.Loading, selectedChapter = null)
        runCatching { repository.bookDetail(bookId) }
            .onSuccess { mutable.value = mutable.value.copy(selectedBook = UiState.Success(it)) }
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

    fun openChapter(bookId: Long, chapterId: Long) = viewModelScope.launch {
        mutable.value = mutable.value.copy(selectedChapter = UiState.Loading)
        runCatching { repository.chapterDetail(chapterId) }
            .onSuccess {
                val userId = mutable.value.session.userId
                repository.saveProgress(userId, bookId, chapterId, 0)
                val marks = repository.chapterMarks(userId, chapterId)
                mutable.value = mutable.value.copy(selectedChapter = UiState.Success(it), chapterMarks = marks)
            }
            .onFailure { mutable.value = mutable.value.copy(selectedChapter = UiState.Error(it.toUserMessage())) }
    }

    fun closeChapter() {
        mutable.value = mutable.value.copy(selectedChapter = null, chapterMarks = emptyList())
    }

    fun saveParagraphMark(paragraphId: Long, paragraphSeq: Int, note: String? = null) = viewModelScope.launch {
        val chapter = (mutable.value.selectedChapter as? UiState.Success)?.data ?: return@launch
        val userId = mutable.value.session.userId
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

    fun setFontScale(scale: Float) = viewModelScope.launch {
        repository.saveFontScale(scale)
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
                val refs = answer.references.take(2).joinToString("\n") { "第${it.paragraphSeq}段: ${it.content}" }
                mutable.value = mutable.value.copy(aiResult = "${answer.answer}\n\n$refs")
            }
            .onFailure { mutable.value = mutable.value.copy(aiResult = it.toUserMessage()) }
    }

    fun consumeNotice() {
        mutable.value = mutable.value.copy(notice = null)
    }

    private fun setNotice(message: String) {
        mutable.value = mutable.value.copy(notice = message)
    }
}
