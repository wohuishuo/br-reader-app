package com.bookrealm.reader.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bookrealm.reader.core.UiState
import com.bookrealm.reader.ui.component.StateBox
import com.bookrealm.reader.ui.screen.BookDetailScreen
import com.bookrealm.reader.ui.screen.MeScreen
import com.bookrealm.reader.ui.screen.ReaderScreen
import com.bookrealm.reader.ui.screen.ShelfScreen
import com.bookrealm.reader.ui.screen.StoreScreen
import com.bookrealm.reader.ui.theme.ReaderTheme
import com.bookrealm.reader.viewmodel.ReaderUiState
import com.bookrealm.reader.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current.findActivity()
    var lastShelfBackAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state.notice) {
        state.notice?.let {
            snackbarHostState.showSnackbar(it)
            actions.consumeNotice()
        }
    }

    val immersive = state.selectedChapter != null
    SystemBarsEffect(immersive = immersive)

    BackHandler {
        when {
            state.selectedChapter != null -> actions.closeChapter()
            state.selectedBook != null -> actions.closeBook()
            currentRoute != "shelf" -> navController.navigate("shelf") {
                popUpTo("shelf")
                launchSingleTop = true
            }
            System.currentTimeMillis() - lastShelfBackAt < 1800 -> activity?.finish()
            else -> {
                lastShelfBackAt = System.currentTimeMillis()
                scope.launch { snackbarHostState.showSnackbar("再按一次退出书域") }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!immersive) {
                TopAppBar(
                    title = { Text(currentRoute.title()) },
                    actions = {
                        if (currentRoute == "shelf") {
                            TextButton(onClick = {
                                actions.closeBook()
                                navController.navigate("store") {
                                    popUpTo("shelf")
                                    launchSingleTop = true
                                }
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "找书")
                                Text("找书")
                            }
                        }
                        if (currentRoute == "me" && state.session.token.isNotBlank()) {
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
                                actions.closeBook()
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
        when {
            state.selectedChapter != null -> {
                ReaderScreen(
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
            }
            state.selectedBook != null -> {
                val selectedBookId = (state.selectedBook as? UiState.Success)?.data?.id
                BookDetailScreen(
                    state = state.selectedBook,
                    session = state.session,
                    onBack = actions::closeBook,
                    onRetry = { selectedBookId?.let(actions::openBook) },
                    onAddShelf = actions::addToShelf,
                    onOpenChapter = actions::openChapter,
                    modifier = Modifier.padding(padding),
                )
            }
            else -> {
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
}

private fun String.title(): String = when (this) {
    "store" -> "书城"
    "me" -> "我的"
    else -> "书架"
}

@Composable
private fun SystemBarsEffect(immersive: Boolean) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return

    DisposableEffect(immersive) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, !immersive)
        val controller = WindowInsetsControllerCompat(activity.window, view)
        if (immersive) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
        onDispose {
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    ReaderTheme {
        StateBox("书域阅读", "v2.1 第二轮把页面、组件和状态层拆清楚。")
    }
}
