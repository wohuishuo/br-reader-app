package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.bookrealm.reader.ui.design.BrButton
import com.bookrealm.reader.ui.design.BrDimens
import com.bookrealm.reader.ui.design.BrSettingSwatch
import com.bookrealm.reader.ui.design.BrTextField
import com.bookrealm.reader.ui.design.InfoCard
import com.bookrealm.reader.ui.design.QuickEntryGrid

@Composable
fun MeScreen(
    loggedIn: Boolean,
    account: String,
    username: String,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onDarkTheme: (Boolean) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
) {
    var userAccount by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("12345678") }
    Column(
        Modifier.fillMaxSize().padding(BrDimens.PagePaddingLarge),
        verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg),
        horizontalAlignment = Alignment.Start,
    ) {
        if (loggedIn) {
            InfoCard(title = "账户") {
                Text("已登录", color = MaterialTheme.colorScheme.primary)
                Text(username.ifBlank { account }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                BrButton(text = "退出登录", onClick = onLogout, tonal = true)
            }
            InfoCard(title = "外观") {
                Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                    BrSettingSwatch("深色", selected = darkTheme, onClick = { onDarkTheme(true) }, modifier = Modifier.weight(1f))
                    BrSettingSwatch("浅色", selected = !darkTheme, onClick = { onDarkTheme(false) }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(BrDimens.GapSm)) {
                    BrSettingSwatch("品牌色", selected = !dynamicColor, onClick = { onDynamicColor(false) }, modifier = Modifier.weight(1f))
                    BrSettingSwatch("系统色", selected = dynamicColor, onClick = { onDynamicColor(true) }, modifier = Modifier.weight(1f))
                }
            }
            QuickEntryGrid(
                entries = listOf(
                    "阅读统计" to "查看最近阅读进度",
                    "我的笔记" to "划线与想法入口",
                    "导入任务" to "PDF/EPUB 后续进入",
                    "朗读缓存" to "TTS 与离线内容",
                ),
                onEntryClick = {},
            )
        } else {
            Text("登录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            BrTextField(value = userAccount, onValueChange = { userAccount = it }, label = "账号")
            BrTextField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                visualTransformation = PasswordVisualTransformation(),
            )
            BrButton(
                text = "登录",
                onClick = { onLogin(userAccount, password) },
                icon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
            )
            Text("默认演示账号 root / 12345678。")
        }
    }
}
