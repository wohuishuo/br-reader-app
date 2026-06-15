package com.bookrealm.reader.ui.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
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
fun BrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = visualTransformation,
        shape = BrShapes.Md,
        modifier = modifier,
    )
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
fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = BrShapes.Md) {
        Column(Modifier.padding(BrDimens.GapLg), verticalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
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

@Composable
fun BrActionDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(shape = BrShapes.Xl, color = BrColors.ActionDock, tonalElevation = BrDimens.GapSm, modifier = modifier) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = BrDimens.GapMd, vertical = BrDimens.GapSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun BrDockAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = BrDimens.GapXs),
    ) {
        Icon(icon, contentDescription = label, tint = Color.White)
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AiPromptChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = BrShapes.Xl,
        color = Color.Transparent,
        border = BorderStroke(BrDimens.Hairline, BrColors.AiBorder),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(text, modifier = Modifier.padding(horizontal = BrDimens.GapLg, vertical = BrDimens.GapMd), color = BrColors.AiTextMuted)
    }
}

@Composable
fun AiInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(shape = BrShapes.Xl, color = BrColors.AiInput, modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = BrDimens.GapLg, top = BrDimens.GapSm, end = BrDimens.GapSm, bottom = BrDimens.GapSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "针对本书提出你的问题",
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(BrDimens.GapSm))
            androidx.compose.material3.FloatingActionButton(onClick = onSend, modifier = Modifier.size(BrDimens.FabSmall)) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = "发送")
            }
        }
    }
}
