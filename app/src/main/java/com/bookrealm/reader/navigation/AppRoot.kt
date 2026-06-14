package com.bookrealm.reader.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.bookrealm.reader.data.remote.dto.BookDetailDto
import com.bookrealm.reader.data.remote.dto.BookItemDto
import com.bookrealm.reader.data.remote.dto.ChapterDetailDto
import com.bookrealm.reader.data.remote.dto.ChapterItemDto
import com.bookrealm.reader.ui.theme.ReaderTheme
import com.bookrealm.reader.viewmodel.ReaderUiState
import com.bookrealm.reader.viewmodel.ReaderViewModel

private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书域阅读") },
                actions = {
                    if (state.session.token.isNotBlank()) {
                        TextButton(onClick = actions::logout) { Text("退出") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
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
    ) { padding ->
        if (state.selectedChapter != null) {
            ChapterScreen(
                state = state.selectedChapter,
                fontScale = state.session.fontScale,
                initialParagraphIndex = state.session.lastParagraphIndex,
                onBack = actions::closeChapter,
                onFont = actions::setFontScale,
                onProgress = actions::saveProgress,
                modifier = Modifier.padding(padding),
            )
        } else if (state.selectedBook != null) {
            BookDetailScreen(
                state = state.selectedBook,
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
                        onOpen = actions::openBook,
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
private fun ShelfScreen(books: List<BookCacheEntity>, onOpen: (Long) -> Unit) {
    if (books.isEmpty()) {
        EmptyState("书架还没有书", "去书城选择一本书，点进详情后加入书架。")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(books, key = { it.id }) { book ->
                BookCard(
                    title = book.title,
                    author = book.author,
                    intro = book.intro,
                    tags = listOf("已加入书架"),
                    onClick = { onOpen(book.id) },
                )
            }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("搜索书名，例如 西游") },
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
        Text("我们的账号", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (loggedIn) {
            Text("已登录: ${username.ifBlank { account }}")
            Text("登录走用户中心，内容走书库服务。这个页面证明 MVP-0 与 MVP-2 已经咬合。")
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
    onBack: () -> Unit,
    onAddShelf: (BookDetailDto) -> Unit,
    onOpenChapter: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
        when (state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> EmptyState("书籍详情加载失败", state.message)
            is UiState.Success -> {
                val book = state.data
                Text(book.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(book.author, color = MaterialTheme.colorScheme.primary)
                Text(book.intro)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    book.tags.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                Button(onClick = { onAddShelf(book) }) { Text("加入书架") }
                Text("章节目录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(book.chapters, key = { it.id }) { chapter ->
                        ChapterRow(chapter) { onOpenChapter(book.id, chapter.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterScreen(
    state: UiState<ChapterDetailDto>,
    fontScale: Float,
    initialParagraphIndex: Int,
    onBack: () -> Unit,
    onFont: (Float) -> Unit,
    onProgress: (Long, Long, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onFont(fontScale - 0.1f) }) {
                Icon(Icons.Filled.TextDecrease, contentDescription = "减小字号")
            }
            IconButton(onClick = { onFont(fontScale + 0.1f) }) {
                Icon(Icons.Filled.TextIncrease, contentDescription = "增大字号")
            }
        }
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
                    onProgress(chapter.bookId, chapter.id, listState.firstVisibleItemIndex)
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Text(chapter.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    items(chapter.paragraphs, key = { it.id }) { paragraph ->
                        Text(
                            paragraph.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * fontScale,
                            )
                        )
                    }
                }
            }
        }
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
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(author, color = MaterialTheme.colorScheme.primary)
            Text(intro, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: ChapterItemDto, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${chapter.seq}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(36.dp))
            Text(chapter.title, style = MaterialTheme.typography.bodyLarge)
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
        EmptyState("书域阅读", "登录、书城、书架、阅读器组成 MVP2 的第一条闭环。")
    }
}
