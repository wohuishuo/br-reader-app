package com.bookrealm.reader.ui.screen

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.data.remote.dto.CommentItemDto
import com.bookrealm.reader.data.remote.dto.MarkItemDto
import com.bookrealm.reader.data.remote.dto.ParagraphDto
import com.bookrealm.reader.ui.component.ChapterRow
import com.bookrealm.reader.ui.component.LoadingBox
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.design.AiInputBar
import com.bookrealm.reader.ui.design.AiPromptChip
import com.bookrealm.reader.ui.design.BrActionDock
import com.bookrealm.reader.ui.design.BrButton
import com.bookrealm.reader.ui.design.BrColors
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.BrDockAction
import com.bookrealm.reader.ui.design.BrReaderBottomSurface
import com.bookrealm.reader.ui.design.BrReaderTopSurface
import com.bookrealm.reader.ui.design.BrShapes
import com.bookrealm.reader.ui.design.BrTextField
import com.bookrealm.reader.ui.design.InfoCard
import com.bookrealm.reader.ui.reader.ReaderPalette
import com.bookrealm.reader.ui.testing.TestTags

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    state: UiState<ChapterDetailDto>,
    fontScale: Float,
    lineScale: Float,
    paletteName: String,
    initialParagraphIndex: Int,
    userId: Long,
    aiResult: String?,
    marks: List<MarkItemDto>,
    comments: List<CommentItemDto>,
    activeInteractionParagraphId: Long?,
    chapters: List<ChapterItemDto>,
    onBack: () -> Unit,
    onFont: (Float) -> Unit,
    onLineScale: (Float) -> Unit,
    onPalette: (String) -> Unit,
    onProgress: (Long, Long, Long, Int) -> Unit,
    onSummary: () -> Unit,
    onAsk: (String) -> Unit,
    onMark: (Long, Int, String?) -> Unit,
    onDeleteMark: (Long) -> Unit,
    onOpenInteraction: (Long) -> Unit,
    onCloseInteraction: () -> Unit,
    onComment: (Long, String) -> Unit,
    onToggleCommentLike: (CommentItemDto) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
) {
    var question by remember { mutableStateOf("仙石是什么") }
    var controlsVisible by remember { mutableStateOf(false) }
    val palette = remember(paletteName) { ReaderPalette.entries.firstOrNull { it.name == paletteName } ?: ReaderPalette.Paper }
    var currentLineScale by remember(lineScale) { mutableFloatStateOf(lineScale) }
    var showToc by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var aiExpanded by remember { mutableStateOf(false) }
    var selectionStartSeq by remember { mutableStateOf<Int?>(null) }
    var selectionEndSeq by remember { mutableStateOf<Int?>(null) }
    var notePanelVisible by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }
    var commentDraft by remember { mutableStateOf("") }
    var activeMark by remember { mutableStateOf<MarkItemDto?>(null) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val markedParagraphs = remember(marks) { marks.map { it.paragraphId }.toSet() }
    val chapterData = (state as? UiState.Success)?.data
    fun toggleControls() {
        val nextVisible = !controlsVisible
        controlsVisible = nextVisible
        if (!nextVisible) aiExpanded = false
    }
    val selectedParagraphs = remember(selectionStartSeq, selectionEndSeq, chapterData?.paragraphs) {
        val start = selectionStartSeq
        val end = selectionEndSeq ?: start
        if (start == null || end == null) {
            emptyList()
        } else {
            val range = minOf(start, end)..maxOf(start, end)
            chapterData?.paragraphs.orEmpty().filter { it.seq in range }
        }
    }

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
                        if (selectionStartSeq != null) {
                            selectionStartSeq = null
                            selectionEndSeq = null
                            notePanelVisible = false
                        } else {
                            toggleControls()
                        }
                    },
                    contentPadding = PaddingValues(
                        start = BrDimens.PagePaddingLarge,
                        top = if (controlsVisible) 88.dp else 44.dp,
                        end = BrDimens.PagePaddingLarge,
                        bottom = if (controlsVisible) 116.dp else 48.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy((14 * currentLineScale).dp),
                ) {
                    item {
                        Text(chapter.title, color = palette.foreground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    items(chapter.paragraphs, key = { it.id }) { paragraph ->
                        ParagraphText(
                            paragraph = paragraph,
                            marked = paragraph.id in markedParagraphs,
                            selected = selectedParagraphs.any { it.id == paragraph.id },
                            palette = palette,
                            fontScale = fontScale,
                            lineScale = currentLineScale,
                            onClick = {
                                val mark = marks.firstOrNull { it.paragraphId == paragraph.id }
                                if (selectionStartSeq != null) {
                                    selectionEndSeq = paragraph.seq
                                } else if (mark != null) {
                                    activeMark = mark
                                    controlsVisible = true
                                } else {
                                    toggleControls()
                                }
                            },
                            onLongClick = {
                                selectionStartSeq = paragraph.seq
                                selectionEndSeq = paragraph.seq
                                notePanelVisible = false
                                noteDraft = marks.firstOrNull { it.paragraphId == paragraph.id }?.note.orEmpty()
                            },
                        )
                    }
                }

                if (controlsVisible) {
                    ReaderTopBar(title = chapter.title, palette = palette, onBack = onBack, onMore = { showSettings = true })
                    ReaderBottomBar(
                        palette = palette,
                        onToc = { showToc = true },
                        onSettings = { showSettings = true },
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

                if (activeInteractionParagraphId != null) {
                    ParagraphInteractionPanel(
                        comments = comments,
                        commentDraft = commentDraft,
                        onCommentChange = { commentDraft = it },
                        onSend = {
                            onComment(activeInteractionParagraphId, commentDraft)
                            commentDraft = ""
                        },
                        onLike = onToggleCommentLike,
                        onClose = onCloseInteraction,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 88.dp),
                    )
                }

                activeMark?.let { mark ->
                    MarkActionPanel(
                        mark = mark,
                        onDelete = {
                            onDeleteMark(mark.id)
                            activeMark = null
                        },
                        onNote = {
                            selectionStartSeq = mark.paragraphSeq
                            selectionEndSeq = mark.paragraphSeq
                            noteDraft = mark.note.orEmpty()
                            notePanelVisible = true
                            activeMark = null
                        },
                        onComment = {
                            onOpenInteraction(mark.paragraphId)
                            activeMark = null
                        },
                        onClose = { activeMark = null },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 88.dp)
                            .testTag(TestTags.ReaderMarkMenu),
                    )
                }
            }
        }

        if (aiExpanded) {
            AiAskFullScreen(
                title = "AI 问书",
                subtitle = chapterData?.title.orEmpty(),
                question = question,
                onQuestion = { question = it },
                aiResult = aiResult,
                onSummary = onSummary,
                onQuickAsk = {
                    question = it
                    onAsk(it)
                },
                onClose = { aiExpanded = false },
                onAsk = {
                    onAsk(question)
                },
            )
        } else if (controlsVisible && selectedParagraphs.isEmpty()) {
            AiAskButton(
                onClick = { aiExpanded = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = if (controlsVisible) 92.dp else 28.dp),
            )
        }

        if (showSettings) {
            ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                ReaderSettings(
                    palette = palette,
                    lineScale = currentLineScale,
                    onPalette = { onPalette(it.name) },
                    onLineScale = {
                        currentLineScale = it
                        onLineScale(it)
                    },
                    onFont = onFont,
                    fontScale = fontScale,
                    modifier = Modifier.testTag(TestTags.ReaderSettings),
                )
            }
        }

        if (selectedParagraphs.isNotEmpty()) {
            SelectionToolbar(
                paragraphs = selectedParagraphs,
                noteDraft = noteDraft,
                notePanelVisible = notePanelVisible,
                onNoteChange = { noteDraft = it },
                onHighlight = {
                    selectedParagraphs.forEach { onMark(it.id, it.seq, null) }
                    selectionStartSeq = null
                    selectionEndSeq = null
                },
                onCopy = {
                    clipboard.setText(AnnotatedString(selectedParagraphs.joinToString("\n") { it.content }))
                    Toast.makeText(context, "已复制 ${selectedParagraphs.size} 段", Toast.LENGTH_SHORT).show()
                },
                onToggleNote = { notePanelVisible = !notePanelVisible },
                onSaveNote = {
                    selectedParagraphs.forEach { onMark(it.id, it.seq, noteDraft) }
                    selectionStartSeq = null
                    selectionEndSeq = null
                },
                onComment = {
                    val paragraph = selectedParagraphs.first()
                    onOpenInteraction(paragraph.id)
                    commentDraft = noteDraft.ifBlank { selectedParagraphs.joinToString("\n") { it.content }.take(120) }
                    selectionStartSeq = null
                    selectionEndSeq = null
                    notePanelVisible = false
                },
                onAsk = {
                    val q = "解释这段话: ${selectedParagraphs.joinToString(" ") { it.content }.take(180)}"
                    question = q
                    aiExpanded = true
                    controlsVisible = true
                    onAsk(q)
                    selectionStartSeq = null
                    selectionEndSeq = null
                },
                onClose = {
                    selectionStartSeq = null
                    selectionEndSeq = null
                    notePanelVisible = false
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 14.dp, end = 14.dp, bottom = 84.dp)
                    .testTag(TestTags.ReaderSelectionToolbar),
            )
        }
    }
}

@Composable
private fun MarkActionPanel(
    mark: MarkItemDto,
    onDelete: () -> Unit,
    onNote: () -> Unit,
    onComment: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(title = if (mark.note.isNullOrBlank()) "已划线" else "已保存想法", modifier = modifier.fillMaxWidth()) {
        if (!mark.note.isNullOrBlank()) {
            Text(mark.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onNote, modifier = Modifier.weight(1f)) { Text("写想法") }
            FilledTonalButton(onClick = onComment, modifier = Modifier.weight(1f)) { Text("段评") }
            FilledTonalButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("删除")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilledTonalButton(onClick = onClose) { Text("收起") }
        }
    }
}

@Composable
private fun SelectionToolbar(
    paragraphs: List<ParagraphDto>,
    noteDraft: String,
    notePanelVisible: Boolean,
    onNoteChange: (String) -> Unit,
    onHighlight: () -> Unit,
    onCopy: () -> Unit,
    onToggleNote: () -> Unit,
    onSaveNote: () -> Unit,
    onComment: () -> Unit,
    onAsk: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (notePanelVisible) {
            InfoCard(title = "写想法") {
                Text(paragraphs.joinToString("\n") { it.content }, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BrTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    label = "笔记",
                    minLines = 2,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    BrButton(text = "保存", onClick = onSaveNote)
                }
            }
        }
        BrActionDock {
            BrDockAction(Icons.Filled.ContentCopy, "复制", onCopy)
            BrDockAction(Icons.Filled.Highlight, "划线", onHighlight)
            BrDockAction(Icons.Filled.EditNote, "写想法", onToggleNote)
            BrDockAction(Icons.Filled.Forum, "段评", onComment)
            BrDockAction(Icons.Filled.Psychology, "AI 问书", onAsk)
            BrDockAction(Icons.Filled.Headphones, "听当前", onClick = {})
            IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ParagraphInteractionPanel(
    comments: List<CommentItemDto>,
    commentDraft: String,
    onCommentChange: (String) -> Unit,
    onSend: () -> Unit,
    onLike: (CommentItemDto) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(title = "段评", modifier = modifier.fillMaxWidth()) {
        if (comments.isEmpty()) {
            Text("还没有段评。我们先写第一条。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            comments.take(4).forEach { comment ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.content, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    FilledTonalButton(onClick = { onLike(comment) }) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(comment.likeCount.toString())
                    }
                }
            }
        }
        BrTextField(
            value = commentDraft,
            onValueChange = onCommentChange,
            label = "写段评",
            minLines = 2,
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilledTonalButton(onClick = onClose) { Text("收起") }
            Spacer(Modifier.width(8.dp))
            BrButton(text = "发布", onClick = onSend, enabled = commentDraft.isNotBlank())
        }
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Text(
        paragraph.content,
        modifier = Modifier
            .testTag(TestTags.ReaderParagraphPrefix + paragraph.seq)
            .background(
                when {
                    selected -> BrColors.Selection
                    marked -> BrColors.Highlight
                    else -> Color.Transparent
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 2.dp),
        color = palette.foreground,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * fontScale * lineScale,
        )
    )
}

@Composable
private fun ReaderTopBar(title: String, palette: ReaderPalette, onBack: () -> Unit, onMore: () -> Unit) {
    BrReaderTopSurface(containerColor = palette.background) {
        Row(
            Modifier
                .fillMaxWidth()
                .displayCutoutPadding()
                .height(BrDimens.ReaderTopBarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = palette.foreground) }
            Text(title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, color = palette.foreground)
            IconButton(onClick = onMore) { Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = palette.foreground) }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    palette: ReaderPalette,
    onToc: () -> Unit,
    onSettings: () -> Unit,
    onListen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        BrReaderBottomSurface(containerColor = palette.background) {
        Row(
            Modifier.fillMaxWidth().height(BrDimens.ReaderBottomBarHeight).padding(horizontal = BrDimens.GapSm),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReaderTool(Icons.AutoMirrored.Filled.MenuBook, "目录", palette, onToc)
            ReaderTool(Icons.Filled.Palette, "设置", palette, onSettings)
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
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
        HorizontalDivider()
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
    FloatingActionButton(onClick = onClick, modifier = modifier.testTag(TestTags.AiAskButton)) {
        Icon(Icons.Filled.Psychology, contentDescription = "AI 提问")
    }
}

@Composable
private fun AiAskFullScreen(
    title: String,
    subtitle: String,
    question: String,
    aiResult: String?,
    onQuestion: (String) -> Unit,
    onSummary: () -> Unit,
    onQuickAsk: (String) -> Unit,
    onClose: () -> Unit,
    onAsk: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = BrColors.AiScrim) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .padding(start = BrDimens.PagePaddingLarge, end = BrDimens.PagePaddingLarge, top = 30.dp, bottom = BrDimens.GapSm),
            verticalArrangement = Arrangement.spacedBy(BrDimens.GapMd),
        ) {
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "关闭 AI", tint = Color.White)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (subtitle.isNotBlank()) {
                        Text("《$subtitle》", color = Color(0xFF8F8F8F), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.width(BrDimens.IconButton))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BrDimens.GapMd),
            ) {
                item {
                    if (!aiResult.isNullOrBlank()) {
                        Surface(shape = BrShapes.Lg, color = BrColors.AiSurface) {
                            Text(aiResult, modifier = Modifier.padding(16.dp), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item {
                    AiPromptChip(text = "生成本章摘要", onClick = onSummary)
                }
                items(
                    listOf(
                        "这段话的重点是什么?",
                        "用三句话总结本章",
                        "列出关键概念",
                    )
                ) { prompt ->
                    AiPromptChip(text = prompt, onClick = { onQuickAsk(prompt) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("亮点", "背景", "概念").forEach { chip ->
                            BrButton(text = chip, onClick = { onQuickAsk(chip) }, modifier = Modifier.weight(1f), tonal = true)
                        }
                    }
                }
            }

            AiInputBar(value = question, onValueChange = onQuestion, onSend = onAsk)
        }
    }
}
