package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.ui.component.BookCover
import com.bookrealm.reader.ui.component.ChapterRow
import com.bookrealm.reader.ui.component.LoadingBox
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.design.BrButton
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.InfoCard

@Composable
fun BookDetailScreen(
    state: UiState<BookDetailDto>,
    session: SessionSnapshot,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddShelf: (BookDetailDto) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        when (state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> StateBox("书籍详情加载失败", state.message, action = "重试", onAction = onRetry)
            is UiState.Success -> {
                val book = state.data
                LazyColumn(contentPadding = PaddingValues(BrDimens.PagePadding), verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg)) {
                    item {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BookCover(title = book.title, modifier = Modifier.size(width = 92.dp, height = 124.dp))
                            Spacer(Modifier.width(BrDimens.GapLg))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                                Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(book.author, color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                                    book.tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                                }
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapMd)) {
                            BrButton(
                                text = if (session.lastBookId == book.id) "继续阅读" else "开始阅读",
                                onClick = { onOpenChapter(book.id, book.chapters.firstOrNull()?.id ?: return@BrButton) },
                                icon = { Icon(Icons.Filled.AutoStories, contentDescription = null) },
                            )
                            BrButton(
                                text = "加入书架",
                                onClick = { onAddShelf(book) },
                                tonal = true,
                                icon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null) },
                            )
                        }
                    }
                    item {
                        DetailSection(title = "简介") {
                            Text(book.intro.ifBlank { "这本书还没有简介。" }, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        DetailSection(title = "AI 阅读") {
                            Text("v2.1 先保留入口:进入章节后可以摘要、提问。v2.2 会接选中文字解释。")
                        }
                    }
                    item {
                        Text("目录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    items(book.chapters, key = { it.id }) { chapter ->
                        ChapterRow(chapter) { onOpenChapter(book.id, chapter.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    InfoCard(title = title, content = content)
}
