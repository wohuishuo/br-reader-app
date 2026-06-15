package com.bookrealm.reader.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.BrShapes

@Composable
fun ShelfBookRow(
    book: BookCacheEntity,
    isLast: Boolean,
    onRead: () -> Unit,
    onDetail: () -> Unit,
) {
    Card(shape = BrShapes.Md) {
        Row(Modifier.fillMaxWidth().padding(BrDimens.GapMd), verticalAlignment = Alignment.CenterVertically) {
            BookCover(
                title = book.title,
                compact = true,
                modifier = Modifier
                    .size(width = BrDimens.BookCoverWidth, height = BrDimens.BookCoverHeight)
                    .clickable(onClick = onRead),
            )
            Spacer(Modifier.width(BrDimens.GapMd))
            Column(
                Modifier.weight(1f).clickable(onClick = onRead),
                verticalArrangement = Arrangement.spacedBy(BrDimens.GapXs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (isLast) {
                        Spacer(Modifier.width(BrDimens.GapXs))
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(book.author, color = MaterialTheme.colorScheme.primary)
                Text(book.intro, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDetail) {
                Icon(Icons.Filled.Info, contentDescription = "详情")
            }
        }
    }
}

@Composable
fun BookCard(book: BookItemDto, onClick: () -> Unit) {
    Card(onClick = onClick, shape = BrShapes.Md) {
        Row(Modifier.fillMaxWidth().padding(BrDimens.GapMd), verticalAlignment = Alignment.CenterVertically) {
            BookCover(title = book.title, compact = true)
            Spacer(Modifier.width(BrDimens.GapMd))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(book.author, color = MaterialTheme.colorScheme.primary)
                Text(book.intro, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                    book.tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                }
            }
        }
    }
}

@Composable
fun ChapterRow(chapter: ChapterItemDto, onClick: () -> Unit) {
    Card(onClick = onClick, shape = BrShapes.Md) {
        Row(Modifier.fillMaxWidth().padding(BrDimens.GapMd), verticalAlignment = Alignment.CenterVertically) {
            Text("${chapter.seq}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(42.dp))
            Text(chapter.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
    }
}
