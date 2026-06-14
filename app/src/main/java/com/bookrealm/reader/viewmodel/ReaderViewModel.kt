package com.bookrealm.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
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
    val query: String = "",
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
                repository.saveProgress(bookId, chapterId, 0)
                mutable.value = mutable.value.copy(selectedChapter = UiState.Success(it))
            }
            .onFailure { mutable.value = mutable.value.copy(selectedChapter = UiState.Error(it.toUserMessage())) }
    }

    fun closeChapter() {
        mutable.value = mutable.value.copy(selectedChapter = null)
    }

    fun setFontScale(scale: Float) = viewModelScope.launch {
        repository.saveFontScale(scale)
    }

    fun saveProgress(bookId: Long, chapterId: Long, paragraphIndex: Int) = viewModelScope.launch {
        repository.saveProgress(bookId, chapterId, paragraphIndex)
    }

    fun consumeNotice() {
        mutable.value = mutable.value.copy(notice = null)
    }

    private fun setNotice(message: String) {
        mutable.value = mutable.value.copy(notice = message)
    }
}
