package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.ui.component.ShelfBookRow
import com.bookrealm.reader.ui.design.BannerCard
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.MiniPlayerBar
import com.bookrealm.reader.ui.design.SearchEntryCard
import com.bookrealm.reader.ui.design.SectionHeader

@Composable
fun ShelfScreen(
    books: List<BookCacheEntity>,
    session: SessionSnapshot,
    onOpen: (Long) -> Unit,
    onRead: (Long) -> Unit,
    onGoStore: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(BrDimens.PagePadding),
        verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg),
    ) {
        item {
            SearchEntryCard(text = "搜索书名、作者或关键词", onClick = onGoStore)
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
                ContinueReadingCard(book = last, session = session, onOpen = { onOpen(last.id) })
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
        } else {
            items(books, key = { it.id }) { book ->
                ShelfBookRow(
                    book = book,
                    isLast = book.id == session.lastBookId,
                    onRead = { onRead(book.id) },
                    onDetail = { onOpen(book.id) },
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
