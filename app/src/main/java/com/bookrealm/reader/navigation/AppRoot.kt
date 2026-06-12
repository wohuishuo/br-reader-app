package com.bookrealm.reader.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bookrealm.reader.ui.theme.ReaderTheme

/** 底部三 Tab:书架 / 书城 / 我的(R1 骨架,各页由后续工单填实)。 */
private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

private val tabs = listOf(
    Tab("shelf", "书架") { Icon(Icons.Filled.CollectionsBookmark, contentDescription = "书架") },
    Tab("store", "书城") { Icon(Icons.Filled.Storefront, contentDescription = "书城") },
    Tab("me", "我的") { Icon(Icons.Filled.Person, contentDescription = "我的") },
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo("shelf"); launchSingleTop = true
                            }
                        },
                        icon = tab.icon,
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "shelf",
            modifier = Modifier.padding(padding)
        ) {
            composable("shelf") { PlaceholderScreen("书架(R4 实现)") }
            composable("store") { PlaceholderScreen("书城(R4 实现)") }
            composable("me") { PlaceholderScreen("我的 · 登录入口(R3 实现)") }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text) }
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    ReaderTheme { AppRoot() }
}
