package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
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
import com.bookrealm.reader.ui.design.BrTextField
import com.bookrealm.reader.ui.design.InfoCard
import com.bookrealm.reader.ui.design.QuickEntryGrid

@Composable
fun MeScreen(
    loggedIn: Boolean,
    account: String,
    username: String,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
) {
    var userAccount by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("12345678") }
    Column(
        Modifier.fillMaxSize().padding(BrDimens.PagePaddingLarge),
        verticalArrangement = Arrangement.spacedBy(BrDimens.GapLg),
        horizontalAlignment = Alignment.Start,
    ) {
        if (loggedIn) {
            InfoCard(title = username.ifBlank { account }) {
                Text("已登录", color = MaterialTheme.colorScheme.primary)
                Text("阅读统计、笔记、导入任务和设置从这里进入。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                BrButton(text = "退出登录", onClick = onLogout, tonal = true)
            }
            QuickEntryGrid(
                entries = listOf(
                    "阅读统计" to "查看最近阅读进度",
                    "我的笔记" to "划线与想法入口",
                    "导入任务" to "PDF/EPUB 后续进入",
                    "设置" to "主题、朗读、缓存",
                ),
                onEntryClick = {},
            )
        } else {
            Text("登录书域", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
