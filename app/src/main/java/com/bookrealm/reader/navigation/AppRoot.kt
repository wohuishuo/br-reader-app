package com.bookrealm.reader.navigation

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.data.local.BookCacheEntity
import com.bookrealm.reader.data.local.SessionSnapshot
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.ui.theme.ReaderTheme
import com.bookrealm.reader.viewmodel.ReaderUiState
import com.bookrealm.reader.viewmodel.ReaderViewModel

private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

private enum class ReaderPalette(
    val label: String,
    val background: Color,
    val foreground: Color,
    val muted: Color,
) {
    Paper("纸页", Color(0xFFFBF8F1), Color(0xFF1F1B16), Color(0xFF7A7165)),
    Green("护眼", Color(0xFFEAF2E6), Color(0xFF1D241A), Color(0xFF68735F)),
    Night("夜间", Color(0xFF141414), Color(0xFFE7E0D6), Color(0xFFAAA197)),
}

private val tabs = listOf(
    Tab("shelf", "书架") { Icon(Icons.Filled.CollectionsBookmark, contentDescription = "书架") },
    Tab("store", "书城") { Icon(Icons.Filled.Storefront, contentDescription = "书城") },
    Tab("me", "我的") { Icon(Icons.Filled.Person, contentDescription = "我的") },
)

@Composable
fun AppRoot(viewModel: ReaderViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppRootContent(state, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRootContent(state: ReaderUiState, actions: ReaderViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "shelf"
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.notice) {
        state.notice?.let {
            snackbarHostState.showSnackbar(it)
            actions.consumeNotice()
        }
    }

    val immersive = state.selectedChapter != null

    Scaffold(
        topBar = {
            if (!immersive) {
                TopAppBar(
                    title = { Text("书域阅读") },
                    actions = {
                        if (state.session.token.isNotBlank()) {
                            TextButton(onClick = actions::logout) { Text("退出") }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!immersive) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("shelf")
                                    launchSingleTop = true
                                }
                            },
                            icon = tab.icon,
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.selectedChapter != null) {
            ChapterScreen(
                state = state.selectedChapter,
                fontScale = state.session.fontScale,
                initialParagraphIndex = state.session.lastParagraphIndex,
                userId = state.session.userId,
                aiResult = state.aiResult,
                chapters = (state.selectedBook as? UiState.Success)?.data?.chapters.orEmpty(),
                onBack = actions::closeChapter,
                onFont = actions::setFontScale,
                onProgress = actions::saveProgress,
                onSummary = actions::summarizeCurrentChapter,
                onAsk = actions::askCurrentChapter,
                onOpenChapter = actions::openChapter,
            )
        } else if (state.selectedBook != null) {
            BookDetailScreen(
                state = state.selectedBook,
                session = state.session,
                onBack = actions::closeBook,
                onAddShelf = actions::addToShelf,
                onOpenChapter = actions::openChapter,
                modifier = Modifier.padding(padding),
            )
        } else {
            NavHost(navController = navController, startDestination = "shelf", modifier = Modifier.padding(padding)) {
                composable("shelf") {
                    ShelfScreen(
                        books = state.shelf,
                        session = state.session,
                        onOpen = actions::openBook,
                        onGoStore = { navController.navigate("store") },
                    )
                }
                composable("store") {
                    StoreScreen(
                        books = state.books,
                        query = state.query,
                        onSearch = actions::refreshBooks,
                        onOpen = actions::openBook,
                    )
                }
                composable("me") {
                    MeScreen(
                        loggedIn = state.session.token.isNotBlank(),
                        account = state.session.account,
                        username = state.session.username,
                        onLogin = actions::login,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelfScreen(
    books: List<BookCacheEntity>,
    session: SessionSnapshot,
    onOpen: (Long) -> Unit,
    onGoStore: () -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("继续阅读", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的书架", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onGoStore) { Text("找书") }
            }
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
private fun ShelfBookRow(book: BookCacheEntity, isLast: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(title = book.title, compact = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (isLast) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(book.author, color = MaterialTheme.colorScheme.primary)
                Text(book.intro, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onClick) { Icon(Icons.Filled.AutoStories, contentDescription = "阅读") }
        }
    }
}

@Composable
private fun StoreScreen(
    books: UiState<List<BookItemDto>>,
    query: String,
    onSearch: (String) -> Unit,
    onOpen: (Long) -> Unit,
) {
    var input by remember(query) { mutableStateOf(query) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("书城", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("搜索书名,例如 西游") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onSearch(input) }) {
                Icon(Icons.Filled.Search, contentDescription = "搜索")
            }
        }
        when (books) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> EmptyState("书城加载失败", books.message)
            is UiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(books.data, key = { it.id }) { book ->
                    BookCard(book.title, book.author, book.intro, book.tags, onClick = { onOpen(book.id) })
                }
            }
        }
    }
}

@Composable
private fun MeScreen(
    loggedIn: Boolean,
    account: String,
    username: String,
    onLogin: (String, String) -> Unit,
) {
    var userAccount by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("12345678") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text("我的", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (loggedIn) {
            Card {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("已登录", color = MaterialTheme.colorScheme.primary)
                    Text(username.ifBlank { account }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("v2 后续会在这里放阅读统计、成就、导入任务和会员能力。")
                }
            }
        } else {
            OutlinedTextField(value = userAccount, onValueChange = { userAccount = it }, label = { Text("账号") })
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
            )
            Button(onClick = { onLogin(userAccount, password) }) {
                Icon(Icons.Filled.Login, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("登录")
            }
            Text("默认演示账号 root / 12345678。")
        }
    }
}

@Composable
private fun BookDetailScreen(
    state: UiState<BookDetailDto>,
    session: SessionSnapshot,
    onBack: () -> Unit,
    onAddShelf: (BookDetailDto) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        when (state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> EmptyState("书籍详情加载失败", state.message)
            is UiState.Success -> {
                val book = state.data
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BookCover(title = book.title, modifier = Modifier.size(width = 92.dp, height = 124.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(book.author, color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    book.tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                                }
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { onOpenChapter(book.id, book.chapters.firstOrNull()?.id ?: return@Button) }) {
                                Icon(Icons.Filled.AutoStories, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (session.lastBookId == book.id) "继续阅读" else "开始阅读")
                            }
                            FilledTonalButton(onClick = { onAddShelf(book) }) {
                                Icon(Icons.Filled.BookmarkAdd, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("加入书架")
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterScreen(
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
            is UiState.Error -> EmptyState("章节加载失败", state.message)
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.background.copy(alpha = 0.97f),
        shadowElevation = 6.dp,
    ) {
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
private fun ReaderTool(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, palette: ReaderPalette, onClick: () -> Unit) {
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
        Text("竖排/分页模式会在 v2.1 第二轮进入;本轮先把工具层和阅读设置跑通。", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingStepper(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: Float, onMinus: () -> Unit, onPlus: () -> Unit) {
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
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), tonalElevation = 4.dp) {
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

@Composable
private fun BookCard(
    title: String,
    author: String,
    intro: String,
    tags: List<String>,
    onClick: () -> Unit,
) {
    Card(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(title = title, compact = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(author, color = MaterialTheme.colorScheme.primary)
                Text(intro, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                }
            }
        }
    }
}

@Composable
private fun BookCover(title: String, modifier: Modifier = Modifier.size(width = 70.dp, height = 96.dp), compact: Boolean = false) {
    val text = title.take(2).ifBlank { "书" }
    Box(
        modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ChapterRow(chapter: ChapterItemDto, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${chapter.seq}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(42.dp))
            Text(chapter.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun EmptyCard(title: String, body: String, action: String, onAction: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(body)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    ReaderTheme {
        EmptyState("书域阅读", "v2.1 先把书架、详情页和阅读器工具层做成真实产品骨架。")
    }
}
