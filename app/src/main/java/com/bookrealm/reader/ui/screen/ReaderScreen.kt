package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Highlight
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.data.remote.dto.MarkItemDto
import com.bookrealm.reader.data.remote.dto.ParagraphDto
import com.bookrealm.reader.ui.component.ChapterRow
import com.bookrealm.reader.ui.component.LoadingBox
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.reader.ReaderPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    state: UiState<ChapterDetailDto>,
    fontScale: Float,
    initialParagraphIndex: Int,
    userId: Long,
    aiResult: String?,
    marks: List<MarkItemDto>,
    chapters: List<ChapterItemDto>,
    onBack: () -> Unit,
    onFont: (Float) -> Unit,
    onProgress: (Long, Long, Long, Int) -> Unit,
    onSummary: () -> Unit,
    onAsk: (String) -> Unit,
    onMark: (Long, Int, String?) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
) {
    var question by remember { mutableStateOf("仙石是什么") }
    var controlsVisible by remember { mutableStateOf(true) }
    var palette by remember { mutableStateOf(ReaderPalette.Paper) }
    var lineScale by remember { mutableFloatStateOf(1.0f) }
    var showToc by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var aiExpanded by remember { mutableStateOf(false) }
    var selectedParagraph by remember { mutableStateOf<ParagraphDto?>(null) }
    var notePanelVisible by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val markedParagraphs = remember(marks) { marks.map { it.paragraphId }.toSet() }

    BackHandler(enabled = aiExpanded) {
        aiExpanded = false
    }

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
                    modifier = Modifier.fillMaxSize().clickable {
                        selectedParagraph = null
                        notePanelVisible = false
                        controlsVisible = !controlsVisible
                        if (!controlsVisible) aiExpanded = false
                    },
                    contentPadding = PaddingValues(start = 22.dp, top = 112.dp, end = 22.dp, bottom = 128.dp),
                    verticalArrangement = Arrangement.spacedBy((14 * lineScale).dp),
                ) {
                    item {
                        Text(chapter.title, color = palette.foreground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    if (!aiResult.isNullOrBlank()) {
                        item { AiResultCard(aiResult = aiResult) }
                    }
                    items(chapter.paragraphs, key = { it.id }) { paragraph ->
                        ParagraphText(
                            paragraph = paragraph,
                            marked = paragraph.id in markedParagraphs,
                            selected = selectedParagraph?.id == paragraph.id,
                            palette = palette,
                            fontScale = fontScale,
                            lineScale = lineScale,
                            onLongClick = {
                                selectedParagraph = paragraph
                                notePanelVisible = false
                                noteDraft = marks.firstOrNull { it.paragraphId == paragraph.id }?.note.orEmpty()
                            },
                        )
                    }
                }

                if (controlsVisible) {
                    ReaderTopBar(title = chapter.title, palette = palette, onBack = onBack)
                    ReaderBottomBar(
                        palette = palette,
                        onToc = { showToc = true },
                        onSettings = { showSettings = true },
                        onSummary = {
                            aiExpanded = true
                            onSummary()
                        },
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
            if (aiExpanded) {
                AiAskPanel(
                    question = question,
                    onQuestion = { question = it },
                    aiResult = aiResult,
                    onClose = { aiExpanded = false },
                    onAsk = {
                        onAsk(question)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 82.dp, start = 14.dp, end = 14.dp),
                )
            } else {
                AiAskButton(
                    onClick = { aiExpanded = true },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 92.dp),
                )
            }
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

        if (controlsVisible) {
            selectedParagraph?.let { paragraph ->
                SelectionToolbar(
                    paragraph = paragraph,
                    noteDraft = noteDraft,
                    notePanelVisible = notePanelVisible,
                    onNoteChange = { noteDraft = it },
                    onHighlight = {
                        onMark(paragraph.id, paragraph.seq, null)
                        selectedParagraph = null
                    },
                    onToggleNote = { notePanelVisible = !notePanelVisible },
                    onSaveNote = {
                        onMark(paragraph.id, paragraph.seq, noteDraft)
                        selectedParagraph = null
                    },
                    onAsk = {
                        val q = "解释这段话: ${paragraph.content.take(80)}"
                        question = q
                        aiExpanded = true
                        onAsk(q)
                        selectedParagraph = null
                    },
                    onClose = {
                        selectedParagraph = null
                        notePanelVisible = false
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = 14.dp, end = 14.dp, bottom = 84.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectionToolbar(
    paragraph: ParagraphDto,
    noteDraft: String,
    notePanelVisible: Boolean,
    onNoteChange: (String) -> Unit,
    onHighlight: () -> Unit,
    onToggleNote: () -> Unit,
    onSaveNote: () -> Unit,
    onAsk: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (notePanelVisible) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("写想法", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(paragraph.content, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = onNoteChange,
                        label = { Text("笔记") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = onSaveNote) { Text("保存") }
                    }
                }
            }
        }
        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xEE2F2F2F), tonalElevation = 8.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionTool(Icons.Filled.ContentCopy, "复制") {}
                SelectionTool(Icons.Filled.Highlight, "划线", onHighlight)
                SelectionTool(Icons.Filled.EditNote, "写想法", onToggleNote)
                SelectionTool(Icons.Filled.Psychology, "AI 问书", onAsk)
                SelectionTool(Icons.Filled.Headphones, "听当前") {}
                IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SelectionTool(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp)) {
        Icon(icon, contentDescription = label, tint = Color.White)
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParagraphText(
    paragraph: ParagraphDto,
    marked: Boolean,
    selected: Boolean,
    palette: ReaderPalette,
    fontScale: Float,
    lineScale: Float,
    onLongClick: () -> Unit,
) {
    Text(
        paragraph.content,
        modifier = Modifier
            .background(
                when {
                    selected -> Color(0x663C8DFF)
                    marked -> Color(0x33FED766)
                    else -> Color.Transparent
                }
            )
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(vertical = 2.dp),
        color = palette.foreground,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * fontScale * lineScale,
        )
    )
}

@Composable
private fun ReaderTopBar(title: String, palette: ReaderPalette, onBack: () -> Unit) {
    Surface(color = palette.background.copy(alpha = 0.92f), shadowElevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
    Surface(modifier = modifier.fillMaxWidth().navigationBarsPadding(), color = palette.background.copy(alpha = 0.90f), shadowElevation = 0.dp) {
        Column {
            HorizontalDivider(color = palette.muted.copy(alpha = 0.22f))
        Row(
            Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
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
private fun AiAskButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Filled.Psychology, contentDescription = "AI 提问")
    }
}

@Composable
private fun AiAskPanel(
    question: String,
    aiResult: String?,
    onQuestion: (String) -> Unit,
    onClose: () -> Unit,
    onAsk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth().navigationBarsPadding(), shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("问问原文", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "收起 AI")
                }
            }
            if (!aiResult.isNullOrBlank()) {
                AiResultCard(aiResult = aiResult)
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
private fun AiResultCard(aiResult: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(aiResult, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
