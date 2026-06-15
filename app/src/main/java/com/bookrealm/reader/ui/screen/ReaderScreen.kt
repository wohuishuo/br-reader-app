package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.ui.component.ChapterRow
import com.bookrealm.reader.ui.component.LoadingBox
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.reader.ReaderPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    state: UiState<ChapterDetailDto>,
    fontScale: Float,
    initialParagraphIndex: Int,
    userId: Long,
    aiResult: String?,
    chapters: List<ChapterItemDto>,
    onBack: () -> Unit,
    onFont: (Float) -> Unit,
    onProgress: (Long, Long, Long, Int) -> Unit,
    onSummary: () -> Unit,
    onAsk: (String) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
) {
    var question by remember { mutableStateOf("仙石是什么") }
    var controlsVisible by remember { mutableStateOf(true) }
    var palette by remember { mutableStateOf(ReaderPalette.Paper) }
    var lineScale by remember { mutableFloatStateOf(1.0f) }
    var showToc by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(Modifier.fillMaxSize().background(palette.background)) {
        when (state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> StateBox("章节加载失败", state.message)
            is UiState.Success -> {
                val chapter = state.data
                val listState = rememberLazyListState()
                LaunchedEffect(chapter.id) {
                    if (initialParagraphIndex > 0) {
                        listState.scrollToItem(initialParagraphIndex.coerceAtMost(chapter.paragraphs.lastIndex))
                    }
                }
                LaunchedEffect(listState.firstVisibleItemIndex) {
                    onProgress(userId, chapter.bookId, chapter.id, listState.firstVisibleItemIndex)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().clickable { controlsVisible = !controlsVisible },
                    contentPadding = PaddingValues(start = 22.dp, top = 88.dp, end = 22.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy((14 * lineScale).dp),
                ) {
                    item {
                        Text(chapter.title, color = palette.foreground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    if (!aiResult.isNullOrBlank()) {
                        item { AiResultCard(aiResult = aiResult) }
                    }
                    items(chapter.paragraphs, key = { it.id }) { paragraph ->
                        Text(
                            paragraph.content,
                            color = palette.foreground,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * fontScale * lineScale,
                            )
                        )
                    }
                }

                if (controlsVisible) {
                    ReaderTopBar(title = chapter.title, palette = palette, onBack = onBack)
                    ReaderBottomBar(
                        palette = palette,
                        onToc = { showToc = true },
                        onSettings = { showSettings = true },
                        onSummary = onSummary,
                        onListen = { question = "请朗读当前章节"; onAsk("请用一句话说明本章适合怎样朗读") },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                if (showToc) {
                    ModalBottomSheet(onDismissRequest = { showToc = false }, sheetState = sheetState) {
                        ChapterToc(
                            chapters = chapters.ifEmpty { listOf(ChapterItemDto(chapter.id, chapter.seq, chapter.title)) },
                            onOpen = {
                                showToc = false
                                onOpenChapter(chapter.bookId, it)
                            }
                        )
                    }
                }
            }
        }

        if (controlsVisible) {
            AiAskBar(
                question = question,
                onQuestion = { question = it },
                onAsk = { onAsk(question) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 74.dp, start = 14.dp, end = 14.dp),
            )
        }

        if (showSettings) {
            ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                ReaderSettings(
                    palette = palette,
                    lineScale = lineScale,
                    onPalette = { palette = it },
                    onLineScale = { lineScale = it },
                    onFont = onFont,
                    fontScale = fontScale,
                )
            }
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, palette: ReaderPalette, onBack: () -> Unit) {
    Surface(color = palette.background.copy(alpha = 0.96f), shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = palette.foreground) }
            Text(title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, color = palette.foreground)
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = palette.foreground) }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    palette: ReaderPalette,
    onToc: () -> Unit,
    onSettings: () -> Unit,
    onSummary: () -> Unit,
    onListen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = palette.background.copy(alpha = 0.97f), shadowElevation = 6.dp) {
        Row(
            Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReaderTool(Icons.Filled.MenuBook, "目录", palette, onToc)
            ReaderTool(Icons.Filled.Palette, "设置", palette, onSettings)
            ReaderTool(Icons.Filled.Psychology, "摘要", palette, onSummary)
            ReaderTool(Icons.Filled.Headphones, "听", palette, onListen)
        }
    }
}

@Composable
private fun ReaderTool(icon: ImageVector, text: String, palette: ReaderPalette, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(6.dp)) {
        Icon(icon, contentDescription = text, tint = palette.foreground)
        Text(text, color = palette.muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ReaderSettings(
    palette: ReaderPalette,
    lineScale: Float,
    fontScale: Float,
    onPalette: (ReaderPalette) -> Unit,
    onLineScale: (Float) -> Unit,
    onFont: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("阅读设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReaderPalette.entries.forEach { item ->
                FilledTonalButton(onClick = { onPalette(item) }) {
                    Icon(if (item == ReaderPalette.Night) Icons.Filled.Nightlight else Icons.Filled.WbSunny, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(item.label)
                }
            }
        }
        Divider()
        SettingStepper("字号", Icons.Filled.FormatSize, fontScale, onMinus = { onFont(fontScale - 0.1f) }, onPlus = { onFont(fontScale + 0.1f) })
        SettingStepper("行距", Icons.Filled.FormatLineSpacing, lineScale, onMinus = { onLineScale((lineScale - 0.1f).coerceIn(0.9f, 1.5f)) }, onPlus = { onLineScale((lineScale + 0.1f).coerceIn(0.9f, 1.5f)) })
        Text("竖排/分页模式会在后续 Spike 进入;本轮先把工具层和阅读设置跑通。", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingStepper(label: String, icon: ImageVector, value: Float, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onMinus) { Icon(Icons.Filled.TextDecrease, contentDescription = "减小$label") }
        Text(String.format("%.1f", value))
        IconButton(onClick = onPlus) { Icon(Icons.Filled.TextIncrease, contentDescription = "增大$label") }
    }
}

@Composable
private fun ChapterToc(chapters: List<ChapterItemDto>, onOpen: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().fillMaxHeight(0.45f).padding(16.dp)) {
        Text("目录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chapters, key = { it.id }) { chapter ->
                ChapterRow(chapter) { onOpen(chapter.id) }
            }
        }
    }
}

@Composable
private fun AiAskBar(question: String, onQuestion: (String) -> Unit, onAsk: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
        Row(Modifier.padding(start = 14.dp, top = 6.dp, end = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = question,
                onValueChange = onQuestion,
                label = { Text("问一句") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onAsk) { Text("问") }
        }
    }
}

@Composable
private fun AiResultCard(aiResult: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(aiResult, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
