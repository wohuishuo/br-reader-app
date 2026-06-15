package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.ui.component.BookCover
import com.bookrealm.reader.ui.component.ShelfBookRow
import com.bookrealm.reader.ui.design.BannerCard
import com.bookrealm.reader.ui.design.BrButton
import com.bookrealm.reader.ui.design.SectionHeader
import com.bookrealm.reader.ui.theme.ReaderTokens

@Composable
fun ShelfScreen(
    books: List<BookCacheEntity>,
    session: SessionSnapshot,
    onOpen: (Long) -> Unit,
    onGoStore: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(ReaderTokens.PagePadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
                    body = "v2.1 先让书架成为继续阅读入口,推荐和活动以后再进来。",
                    action = "去书城添加",
                    onAction = onGoStore,
                )
            }
        } else {
            items(books, key = { it.id }) { book ->
                ShelfBookRow(book = book, isLast = book.id == session.lastBookId, onClick = { onOpen(book.id) })
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(book: BookCacheEntity, session: SessionSnapshot, onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(title = book.title)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(book.author, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                val progress = if (session.lastChapterId > 0) {
                    "上次读到第 ${session.lastParagraphIndex + 1} 段"
                } else {
                    "从第一章开始"
                }
                Text(progress, style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onOpen) { Text("继续") }
        }
    }
}

@Composable
private fun EmptyCard(title: String, body: String, action: String, onAction: () -> Unit) {
    BannerCard(title = title, body = body, action = action, onAction = onAction)
}
