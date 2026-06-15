package com.bookrealm.reader.ui.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (action != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSearch) {
            Icon(Icons.Filled.Search, contentDescription = "搜索")
        }
    }
}

@Composable
fun BrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(BrDimens.GapSm))
        }
        Text(text)
    }
    if (tonal) {
        FilledTonalButton(onClick = onClick, modifier = modifier, content = content)
    } else {
        Button(onClick = onClick, modifier = modifier, content = content)
    }
}

@Composable
fun BannerCard(
    title: String?,
    body: String?,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    if (title.isNullOrBlank() && body.isNullOrBlank()) return
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = BrShapes.Md,
    ) {
        Column(Modifier.padding(BrDimens.GapLg), verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
            if (!title.isNullOrBlank()) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (!body.isNullOrBlank()) {
                Text(body, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f))
            }
            if (action != null && onAction != null) {
                BrButton(text = action, onClick = onAction, tonal = true)
            }
        }
    }
}

@Composable
fun EntryCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = modifier, shape = BrShapes.Md) {
        Column(Modifier.padding(BrDimens.GapMd), verticalArrangement = Arrangement.spacedBy(BrDimens.GapXs)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickEntryGrid(
    entries: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    onEntryClick: (String) -> Unit,
) {
    if (entries.isEmpty()) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
        entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                row.forEach { entry ->
                    EntryCard(
                        title = entry.first,
                        body = entry.second,
                        modifier = Modifier.weight(1f),
                        onClick = { onEntryClick(entry.first) },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    title: String?,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (title.isNullOrBlank()) return
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = BrShapes.Xl) {
        Row(Modifier.padding(BrDimens.GapMd), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(BrDimens.GapMd))
            Column(Modifier.weight(1f)) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
