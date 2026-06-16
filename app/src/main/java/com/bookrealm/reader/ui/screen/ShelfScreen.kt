package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.ui.component.ShelfBookRow
import com.bookrealm.reader.ui.design.BannerCard
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.MiniPlayerBar
import com.bookrealm.reader.ui.design.SearchField
import com.bookrealm.reader.ui.design.SectionHeader
import com.bookrealm.reader.ui.testing.TestTags

@Composable
fun ShelfScreen(
    books: List<BookCacheEntity>,
    session: SessionSnapshot,
    onOpen: (Long) -> Unit,
    onRead: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onGoStore: () -> Unit,
) {
    var shelfQuery by remember { mutableStateOf("") }
    val filteredBooks = remember(books, shelfQuery) {
        val q = shelfQuery.trim()
        if (q.isBlank()) {
            books
        } else {
            books.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.author.contains(q, ignoreCase = true) ||
                    it.intro.contains(q, ignoreCase = true)
            }
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(BrDimens.PagePadding),
        verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                SearchField(
                    value = shelfQuery,
                    onValueChange = { shelfQuery = it },
                    onSearch = {},
                    placeholder = "搜索我的书架",
                    modifier = Modifier.testTag(TestTags.ShelfSearch),
                )
                SectionHeader("书城", action = "去找书", onAction = onGoStore)
            }
        }
        item {
            SectionHeader("继续阅读")
        }
        item {
            val last = books.firstOrNull { it.id == session.lastBookId } ?: books.firstOrNull()
            if (last == null) {
                EmptyCard(
                    title = "还没有最近阅读",
                    body = "去书城选择一本书,加入书架后再回来继续读。",
                    action = "去书城",
                    onAction = onGoStore,
                )
            } else {
                ContinueReadingCard(book = last, session = session, onOpen = { onRead(last.id) })
            }
        }
        item {
            SectionHeader("我的书架")
        }
        if (books.isEmpty()) {
            item {
                EmptyCard(
                    title = "书架还没有书",
                    body = "去书城添加一本书后,这里会保存你的阅读进度和离线章节。",
                    action = "去书城添加",
                    onAction = onGoStore,
                )
            }
        } else if (filteredBooks.isEmpty()) {
            item {
                EmptyCard(
                    title = "书架里没有这本书",
                    body = "清空搜索,或去书城搜索全站书库。",
                    action = "去书城搜索",
                    onAction = onGoStore,
                )
            }
        } else {
            items(filteredBooks, key = { it.id }) { book ->
                ShelfBookRow(
                    book = book,
                    isLast = book.id == session.lastBookId,
                    onRead = { onRead(book.id) },
                    onDetail = { onOpen(book.id) },
                    onRemove = { onRemove(book.id) },
                )
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(book: BookCacheEntity, session: SessionSnapshot, onOpen: () -> Unit) {
    val progress = if (session.lastChapterId > 0) {
        "上次读到第 ${session.lastParagraphIndex + 1} 段"
    } else {
        "从第一章开始"
    }
    MiniPlayerBar(title = book.title, subtitle = "${book.author} · $progress", onClick = onOpen)
}

@Composable
private fun EmptyCard(title: String, body: String, action: String, onAction: () -> Unit) {
    BannerCard(title = title, body = body, action = action, onAction = onAction)
}
